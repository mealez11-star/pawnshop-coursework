package ru.mospolytech.pawnshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Модель результата запроса с JOIN таблиц contracts и clients. */
public class ContractSummary {
    private final int contractId;
    private final int clientId;
    private final String clientName;
    private final LocalDate issueDate;
    private final LocalDate returnDueDate;
    private final BigDecimal commissionAmount;
    private final BigDecimal loanAmount;
    private final BigDecimal totalAssessedValue;
    private final int itemCount;

    public ContractSummary(int contractId, int clientId, String clientName,
                           LocalDate issueDate, LocalDate returnDueDate,
                           BigDecimal commissionAmount, BigDecimal loanAmount,
                           BigDecimal totalAssessedValue, int itemCount) {
        this.contractId = contractId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.issueDate = issueDate;
        this.returnDueDate = returnDueDate;
        this.commissionAmount = commissionAmount;
        this.loanAmount = loanAmount;
        this.totalAssessedValue = totalAssessedValue;
        this.itemCount = itemCount;
    }

    public int getContractId() { return contractId; }
    public int getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getReturnDueDate() { return returnDueDate; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public BigDecimal getTotalAssessedValue() { return totalAssessedValue; }
    public int getItemCount() { return itemCount; }
}
