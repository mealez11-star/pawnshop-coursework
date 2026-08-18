package ru.mospolytech.pawnshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Contract {
    private int id;
    private LocalDate issueDate;
    private LocalDate returnDueDate;
    private BigDecimal commissionAmount;
    private BigDecimal loanAmount;
    private int clientId;

    public Contract(int id, LocalDate issueDate, LocalDate returnDueDate,
                    BigDecimal commissionAmount, BigDecimal loanAmount, int clientId) {
        this.id = id;
        this.issueDate = issueDate;
        this.returnDueDate = returnDueDate;
        this.commissionAmount = commissionAmount;
        this.loanAmount = loanAmount;
        this.clientId = clientId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getReturnDueDate() { return returnDueDate; }
    public void setReturnDueDate(LocalDate returnDueDate) { this.returnDueDate = returnDueDate; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
}
