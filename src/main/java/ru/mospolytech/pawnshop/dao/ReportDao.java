package ru.mospolytech.pawnshop.dao;

import ru.mospolytech.pawnshop.config.DatabaseConnection;
import ru.mospolytech.pawnshop.model.FinancialReportRow;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {
    private final DatabaseConnection database = DatabaseConnection.getInstance();

    /** Параметрический запрос финансового отчёта за период. */
    public List<FinancialReportRow> findForPeriod(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT c.id_contract, cl.full_name AS client_name, c.issue_date, "
                + "c.return_due_date, c.loan_amount, c.commission_amount, "
                + "COALESCE(SUM(ci.assessed_value), 0) AS total_assessed_value, "
                + "COUNT(ci.id_item) AS item_count "
                + "FROM contracts c "
                + "JOIN clients cl ON cl.id_client = c.id_client "
                + "LEFT JOIN contract_items ci ON ci.id_contract = c.id_contract "
                + "WHERE c.issue_date BETWEEN ? AND ? "
                + "GROUP BY c.id_contract, cl.full_name, c.issue_date, c.return_due_date, "
                + "c.loan_amount, c.commission_amount "
                + "ORDER BY c.issue_date, c.id_contract";
        List<FinancialReportRow> rows = new ArrayList<>();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(from));
            statement.setDate(2, Date.valueOf(to));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new FinancialReportRow(
                            result.getInt("id_contract"),
                            result.getString("client_name"),
                            result.getDate("issue_date").toLocalDate(),
                            result.getDate("return_due_date").toLocalDate(),
                            result.getBigDecimal("loan_amount"),
                            result.getBigDecimal("commission_amount"),
                            result.getBigDecimal("total_assessed_value"),
                            result.getInt("item_count")
                    ));
                }
            }
        }
        return rows;
    }
}
