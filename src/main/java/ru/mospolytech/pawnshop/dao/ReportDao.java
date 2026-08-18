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
        String sql = "SELECT id_contract, client_name, issue_date, return_due_date, loan_amount, "
                + "commission_amount, total_assessed_value, item_count "
                + "FROM financial_report WHERE issue_date BETWEEN ? AND ? ORDER BY issue_date";
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
