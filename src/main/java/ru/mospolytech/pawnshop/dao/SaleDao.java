package ru.mospolytech.pawnshop.dao;

import ru.mospolytech.pawnshop.config.DatabaseConnection;
import ru.mospolytech.pawnshop.model.Sale;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SaleDao {
    private final DatabaseConnection database = DatabaseConnection.getInstance();

    public List<Sale> findAll() throws SQLException {
        String sql = "SELECT s.id_sale, s.sale_date, s.id_item, i.name AS item_name, "
                + "s.id_price, p.value AS sale_price "
                + "FROM sales s JOIN items i ON i.id_item = s.id_item "
                + "JOIN prices p ON p.id_price = s.id_price ORDER BY s.id_sale";
        List<Sale> sales = new ArrayList<>();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                sales.add(new Sale(
                        result.getInt("id_sale"),
                        result.getDate("sale_date").toLocalDate(),
                        result.getInt("id_item"),
                        result.getString("item_name"),
                        result.getInt("id_price"),
                        result.getBigDecimal("sale_price")
                ));
            }
        }
        return sales;
    }

    /** Продажа и изменение статуса товара выполняются атомарно. */
    public int create(Sale sale) throws SQLException {
        Connection connection = database.getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            ensureItemCanBeSold(connection, sale.getItemId(), sale.getPriceId());
            int id;
            String saleSql = "INSERT INTO sales(sale_date, id_item, id_price) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setDate(1, Date.valueOf(sale.getSaleDate()));
                statement.setInt(2, sale.getItemId());
                statement.setInt(3, sale.getPriceId());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("СУБД не вернула идентификатор продажи");
                    }
                    id = keys.getInt(1);
                }
            }
            updateItemStatus(connection, sale.getItemId(), "SOLD");
            connection.commit();
            return id;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public void update(Sale sale) throws SQLException {
        Connection connection = database.getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            int oldItemId = findItemId(connection, sale.getId());
            if (oldItemId != sale.getItemId()) {
                updateItemStatus(connection, oldItemId, "OWNED_BY_PAWNSHOP");
                ensureItemCanBeSold(connection, sale.getItemId(), sale.getPriceId());
            } else {
                ensurePriceBelongsToItem(connection, sale.getItemId(), sale.getPriceId());
            }

            String sql = "UPDATE sales SET sale_date = ?, id_item = ?, id_price = ? WHERE id_sale = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDate(1, Date.valueOf(sale.getSaleDate()));
                statement.setInt(2, sale.getItemId());
                statement.setInt(3, sale.getPriceId());
                statement.setInt(4, sale.getId());
                statement.executeUpdate();
            }
            updateItemStatus(connection, sale.getItemId(), "SOLD");
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public void delete(int saleId) throws SQLException {
        Connection connection = database.getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            int itemId = findItemId(connection, saleId);
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM sales WHERE id_sale = ?")) {
                statement.setInt(1, saleId);
                statement.executeUpdate();
            }
            updateItemStatus(connection, itemId, "OWNED_BY_PAWNSHOP");
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private void ensureItemCanBeSold(Connection connection, int itemId, int priceId) throws SQLException {
        ensurePriceBelongsToItem(connection, itemId, priceId);
        String sql = "SELECT current_status FROM items WHERE id_item = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, itemId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !"OWNED_BY_PAWNSHOP".equals(result.getString(1))) {
                    throw new SQLException("Продать можно только товар со статусом «Собственность ломбарда»");
                }
            }
        }
    }

    private void ensurePriceBelongsToItem(Connection connection, int itemId, int priceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prices WHERE id_price = ? AND id_item = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, priceId);
            statement.setInt(2, itemId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) == 0) {
                    throw new SQLException("Выбранная цена относится к другому товару");
                }
            }
        }
    }

    private int findItemId(Connection connection, int saleId) throws SQLException {
        String sql = "SELECT id_item FROM sales WHERE id_sale = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, saleId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Продажа не найдена");
                }
                return result.getInt(1);
            }
        }
    }

    private void updateItemStatus(Connection connection, int itemId, String status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE items SET current_status = ? WHERE id_item = ?")) {
            statement.setString(1, status);
            statement.setInt(2, itemId);
            statement.executeUpdate();
        }
    }
}
