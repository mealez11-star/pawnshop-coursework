package ru.mospolytech.pawnshop.dao;

import ru.mospolytech.pawnshop.config.DatabaseConnection;
import ru.mospolytech.pawnshop.model.Contract;
import ru.mospolytech.pawnshop.model.ContractItem;
import ru.mospolytech.pawnshop.model.ContractSummary;
import ru.mospolytech.pawnshop.model.Item;
import ru.mospolytech.pawnshop.model.ItemStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContractDao {
    private final DatabaseConnection database = DatabaseConnection.getInstance();

    public List<ContractSummary> findAllSummaries() throws SQLException {
        String sql = summarySelect() + " ORDER BY c.id_contract";
        return findSummaries(sql, null);
    }

    public List<ContractSummary> findSummariesForUser(int userId) throws SQLException {
        String sql = summarySelect() + " WHERE cl.id_user = ? ORDER BY c.id_contract";
        return findSummaries(sql, userId);
    }

    public int create(Contract contract) throws SQLException {
        String sql = "INSERT INTO contracts(issue_date, return_due_date, commission_amount, loan_amount, id_client) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setContractFields(statement, contract);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("СУБД не вернула идентификатор договора");
                }
                return keys.getInt(1);
            }
        }
    }

    public void update(Contract contract) throws SQLException {
        Connection connection = database.getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            ensureContractEditable(connection, contract.getId());
            ensureContractRemainsLatest(connection, contract);
            String sql = "UPDATE contracts SET issue_date = ?, return_due_date = ?, commission_amount = ?, "
                    + "loan_amount = ?, id_client = ? WHERE id_contract = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setContractFields(statement, contract);
                statement.setInt(6, contract.getId());
                statement.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public void delete(int contractId) throws SQLException {
        Connection connection = database.getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            ensureContractEditable(connection, contractId);
            List<Integer> itemIds = findItemIds(connection, contractId);
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM contracts WHERE id_contract = ?")) {
                statement.setInt(1, contractId);
                statement.executeUpdate();
            }
            for (int itemId : itemIds) {
                restoreStatusAfterRemovingCurrentPledge(connection, itemId);
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public List<ContractItem> findItems(int contractId) throws SQLException {
        String sql = "SELECT ci.id_contract, ci.id_item, i.name, ci.assessed_value "
                + "FROM contract_items ci JOIN items i ON i.id_item = ci.id_item "
                + "WHERE ci.id_contract = ? ORDER BY ci.id_item";
        List<ContractItem> items = new ArrayList<>();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setInt(1, contractId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    items.add(new ContractItem(
                            result.getInt("id_contract"),
                            result.getInt("id_item"),
                            result.getString("name"),
                            result.getBigDecimal("assessed_value")
                    ));
                }
            }
        }
        return items;
    }

    /**
     * Товар разрешено закладывать повторно, но только после возврата клиенту.
     * Добавление товара и смена его текущего статуса выполняются одной транзакцией.
     */
    public void addItem(int contractId, int itemId, BigDecimal assessedValue) throws SQLException {
        Connection connection = database.getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            ensureContractEditable(connection, contractId);
            LocalDate contractIssueDate = findContractIssueDate(connection, contractId);
            ItemStatus status = findItemStatusForUpdate(connection, itemId);
            int contractCount = countItemContracts(connection, itemId);

            if (status == ItemStatus.PLEDGED && contractCount > 0) {
                throw new SQLException("Товар уже находится в действующем договоре");
            }
            if (status == ItemStatus.OWNED_BY_PAWNSHOP || status == ItemStatus.SOLD) {
                throw new SQLException("Собственность ломбарда и проданный товар нельзя заложить повторно");
            }
            if (status == ItemStatus.RETURNED) {
                ensureNewPledgeIsLatest(connection, itemId, contractId, contractIssueDate);
            }

            String sql = "INSERT INTO contract_items(id_contract, id_item, assessed_value) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, contractId);
                statement.setInt(2, itemId);
                statement.setBigDecimal(3, assessedValue);
                statement.executeUpdate();
            }
            if (status == ItemStatus.RETURNED) {
                updateItemStatus(connection, itemId, ItemStatus.PLEDGED);
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public void removeItem(int contractId, int itemId) throws SQLException {
        Connection connection = database.getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            ensureContractEditable(connection, contractId);
            String sql = "DELETE FROM contract_items WHERE id_contract = ? AND id_item = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, contractId);
                statement.setInt(2, itemId);
                if (statement.executeUpdate() == 0) {
                    throw new SQLException("Товар не найден в составе договора");
                }
            }
            restoreStatusAfterRemovingCurrentPledge(connection, itemId);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public void updateAssessedValue(int contractId, int itemId, BigDecimal assessedValue)
            throws SQLException {
        Connection connection = database.getConnection();
        ensureContractEditable(connection, contractId);
        String sql = "UPDATE contract_items SET assessed_value = ? "
                + "WHERE id_contract = ? AND id_item = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, assessedValue);
            statement.setInt(2, contractId);
            statement.setInt(3, itemId);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Товар не найден в составе договора");
            }
        }
    }

    /** Возвращает срок последнего по дате выдачи договора выбранного товара. */
    public Optional<LocalDate> findCurrentReturnDueDateForItem(int itemId) throws SQLException {
        String sql = "SELECT c.return_due_date "
                + "FROM contract_items ci "
                + "JOIN contracts c ON c.id_contract = ci.id_contract "
                + "WHERE ci.id_item = ? "
                + "ORDER BY c.issue_date DESC, c.id_contract DESC LIMIT 1";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setInt(1, itemId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(result.getDate("return_due_date").toLocalDate());
                }
            }
        }
        return Optional.empty();
    }

    /** Товары, которые можно добавить в новый залог. */
    public List<Item> findItemsAvailableForPledge() throws SQLException {
        String sql = "SELECT i.id_item, i.name, i.current_status FROM items i "
                + "WHERE i.current_status = 'RETURNED' "
                + "OR (i.current_status = 'PLEDGED' AND NOT EXISTS "
                + "(SELECT 1 FROM contract_items ci WHERE ci.id_item = i.id_item)) "
                + "ORDER BY i.id_item";
        List<Item> items = new ArrayList<>();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                items.add(new Item(
                        result.getInt("id_item"),
                        result.getString("name"),
                        ItemStatus.valueOf(result.getString("current_status"))
                ));
            }
        }
        return items;
    }

    /** После первого результата по товару договор становится историческим. */
    public boolean isEditable(int contractId) throws SQLException {
        return !isContractLocked(database.getConnection(), contractId);
    }

    private List<ContractSummary> findSummaries(String sql, Integer userId) throws SQLException {
        List<ContractSummary> contracts = new ArrayList<>();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            if (userId != null) {
                statement.setInt(1, userId);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    contracts.add(new ContractSummary(
                            result.getInt("id_contract"),
                            result.getInt("id_client"),
                            result.getString("client_name"),
                            result.getDate("issue_date").toLocalDate(),
                            result.getDate("return_due_date").toLocalDate(),
                            result.getBigDecimal("commission_amount"),
                            result.getBigDecimal("loan_amount"),
                            result.getBigDecimal("total_assessed_value"),
                            result.getInt("item_count")
                    ));
                }
            }
        }
        return contracts;
    }

    private String summarySelect() {
        return "SELECT c.id_contract, c.id_client, cl.full_name AS client_name, c.issue_date, "
                + "c.return_due_date, c.commission_amount, c.loan_amount, "
                + "COALESCE((SELECT SUM(ci.assessed_value) FROM contract_items ci "
                + "WHERE ci.id_contract = c.id_contract), 0) AS total_assessed_value, "
                + "(SELECT COUNT(*) FROM contract_items ci "
                + "WHERE ci.id_contract = c.id_contract) AS item_count "
                + "FROM contracts c JOIN clients cl ON cl.id_client = c.id_client";
    }

    private void setContractFields(PreparedStatement statement, Contract contract) throws SQLException {
        statement.setDate(1, Date.valueOf(contract.getIssueDate()));
        statement.setDate(2, Date.valueOf(contract.getReturnDueDate()));
        statement.setBigDecimal(3, contract.getCommissionAmount());
        statement.setBigDecimal(4, contract.getLoanAmount());
        statement.setInt(5, contract.getClientId());
    }

    private void ensureContractEditable(Connection connection, int contractId) throws SQLException {
        if (isContractLocked(connection, contractId)) {
            throw new SQLException(
                    "Нельзя изменять завершённый или исторический договор и его состав"
            );
        }
    }

    private boolean isContractLocked(Connection connection, int contractId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM contract_items current_ci "
                + "JOIN items i ON i.id_item = current_ci.id_item "
                + "JOIN contracts current_c ON current_c.id_contract = current_ci.id_contract "
                + "WHERE current_ci.id_contract = ? AND (i.current_status <> 'PLEDGED' OR EXISTS ("
                + "SELECT 1 FROM contract_items later_ci "
                + "JOIN contracts later_c ON later_c.id_contract = later_ci.id_contract "
                + "WHERE later_ci.id_item = current_ci.id_item AND ("
                + "later_c.issue_date > current_c.issue_date OR "
                + "(later_c.issue_date = current_c.issue_date AND later_c.id_contract > current_c.id_contract))))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, contractId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    private void ensureContractRemainsLatest(Connection connection, Contract contract) throws SQLException {
        String sql = "SELECT COUNT(*) FROM contract_items current_ci "
                + "JOIN contract_items other_ci ON other_ci.id_item = current_ci.id_item "
                + "JOIN contracts other_c ON other_c.id_contract = other_ci.id_contract "
                + "WHERE current_ci.id_contract = ? AND other_c.id_contract <> ? AND ("
                + "other_c.issue_date > ? OR (other_c.issue_date = ? AND other_c.id_contract > ?))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, contract.getId());
            statement.setInt(2, contract.getId());
            statement.setDate(3, Date.valueOf(contract.getIssueDate()));
            statement.setDate(4, Date.valueOf(contract.getIssueDate()));
            statement.setInt(5, contract.getId());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) > 0) {
                    throw new SQLException(
                            "Дата повторного договора не может быть раньше предыдущего залога товара"
                    );
                }
            }
        }
    }

    private void ensureNewPledgeIsLatest(Connection connection, int itemId, int contractId,
                                         LocalDate contractIssueDate) throws SQLException {
        String sql = "SELECT c.id_contract, c.issue_date FROM contract_items ci "
                + "JOIN contracts c ON c.id_contract = ci.id_contract "
                + "WHERE ci.id_item = ? ORDER BY c.issue_date DESC, c.id_contract DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return;
                int previousContractId = result.getInt("id_contract");
                LocalDate previousIssueDate = result.getDate("issue_date").toLocalDate();
                if (contractIssueDate.isBefore(previousIssueDate)
                        || (contractIssueDate.equals(previousIssueDate) && contractId <= previousContractId)) {
                    throw new SQLException(
                            "Повторный залог нужно добавлять в новый договор, а не в более ранний"
                    );
                }
            }
        }
    }

    private LocalDate findContractIssueDate(Connection connection, int contractId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT issue_date FROM contracts WHERE id_contract = ?")) {
            statement.setInt(1, contractId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("Договор не найден");
                return result.getDate(1).toLocalDate();
            }
        }
    }

    private ItemStatus findItemStatusForUpdate(Connection connection, int itemId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT current_status FROM items WHERE id_item = ? FOR UPDATE")) {
            statement.setInt(1, itemId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("Товар не найден");
                return ItemStatus.valueOf(result.getString(1));
            }
        }
    }

    private int countItemContracts(Connection connection, int itemId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM contract_items WHERE id_item = ?")) {
            statement.setInt(1, itemId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private List<Integer> findItemIds(Connection connection, int contractId) throws SQLException {
        List<Integer> itemIds = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id_item FROM contract_items WHERE id_contract = ?")) {
            statement.setInt(1, contractId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) itemIds.add(result.getInt(1));
            }
        }
        return itemIds;
    }

    private void restoreStatusAfterRemovingCurrentPledge(Connection connection, int itemId)
            throws SQLException {
        if (countItemContracts(connection, itemId) > 0) {
            updateItemStatus(connection, itemId, ItemStatus.RETURNED);
        }
    }

    private void updateItemStatus(Connection connection, int itemId, ItemStatus status)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE items SET current_status = ? WHERE id_item = ?")) {
            statement.setString(1, status.name());
            statement.setInt(2, itemId);
            statement.executeUpdate();
        }
    }
}
