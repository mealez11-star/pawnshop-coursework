package ru.mospolytech.pawnshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Price {
    private int id;
    private LocalDate date;
    private BigDecimal value;
    private int itemId;
    private String itemName;

    public Price(int id, LocalDate date, BigDecimal value, int itemId, String itemName) {
        this.id = id;
        this.date = date;
        this.value = value;
        this.itemId = itemId;
        this.itemName = itemName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    @Override
    public String toString() {
        return id + " - " + itemName + " - " + value;
    }
}
