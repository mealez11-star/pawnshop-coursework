package ru.mospolytech.pawnshop.model;

import java.math.BigDecimal;

public class ContractItem {
    private int contractId;
    private int itemId;
    private String itemName;
    private BigDecimal assessedValue;

    public ContractItem(int contractId, int itemId, String itemName, BigDecimal assessedValue) {
        this.contractId = contractId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.assessedValue = assessedValue;
    }

    public int getContractId() { return contractId; }
    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public BigDecimal getAssessedValue() { return assessedValue; }
}
