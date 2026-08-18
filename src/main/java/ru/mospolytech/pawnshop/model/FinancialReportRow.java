package ru.mospolytech.pawnshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Отдельная ORM-модель для строки составного финансового отчёта. */
public class FinancialReportRow {
    private final int contractId;
    private final String clientName;
    private final LocalDate issueDate;
    private final LocalDate returnDueDate;
    private final BigDecimal loanAmount;
    private final BigDecimal commissionAmount;
    private final BigDecimal totalAssessedValue;
    private final int itemCount;

    public FinancialReportRow(int contractId, String clientName, LocalDate issueDate,
                              LocalDate returnDueDate, BigDecimal loanAmount,
                              BigDecimal commissionAmount, BigDecimal totalAssessedValue,
                              int itemCount) {
        this.contractId = contractId;
        this.clientName = clientName;
        this.issueDate = issueDate;
        this.returnDueDate = returnDueDate;
        this.loanAmount = loanAmount;
        this.commissionAmount = commissionAmount;
        this.totalAssessedValue = totalAssessedValue;
        this.itemCount = itemCount;
    }

    public int getContractId() { return contractId; }
    public String getClientName() { return clientName; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getReturnDueDate() { return returnDueDate; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public BigDecimal getTotalAssessedValue() { return totalAssessedValue; }
    public int getItemCount() { return itemCount; }
}
