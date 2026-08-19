package ru.mospolytech.pawnshop.model;

import java.math.BigDecimal;

/** Итоговые показатели продаж за выбранный период. */
public class SalesReportSummary {
    private final int saleCount;
    private final BigDecimal revenue;

    public SalesReportSummary(int saleCount, BigDecimal revenue) {
        this.saleCount = saleCount;
        this.revenue = revenue;
    }

    public int getSaleCount() { return saleCount; }
    public BigDecimal getRevenue() { return revenue; }
}
