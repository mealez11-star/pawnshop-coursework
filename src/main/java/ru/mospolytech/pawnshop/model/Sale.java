package ru.mospolytech.pawnshop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Sale {
    private int id;
    private LocalDate saleDate;
    private int itemId;
    private String itemName;
    private int priceId;
    private BigDecimal salePrice;

    public Sale(int id, LocalDate saleDate, int itemId, String itemName,
                int priceId, BigDecimal salePrice) {
        this.id = id;
        this.saleDate = saleDate;
        this.itemId = itemId;
        this.itemName = itemName;
        this.priceId = priceId;
        this.salePrice = salePrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getPriceId() { return priceId; }
    public void setPriceId(int priceId) { this.priceId = priceId; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
}
