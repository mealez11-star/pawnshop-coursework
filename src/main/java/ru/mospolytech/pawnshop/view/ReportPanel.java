package ru.mospolytech.pawnshop.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class ReportPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTextField fromField = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
    private final JTextField toField = new JTextField(LocalDate.now().toString(), 10);
    private final JButton buildButton = new JButton("Построить отчёт");
    private final JLabel contractCountLabel = new JLabel("Договоров: 0");
    private final JLabel totalLoansLabel = new JLabel("Выдано: 0,00 ₽");
    private final JLabel totalCommissionLabel = new JLabel("Начисленная комиссия: 0,00 ₽");
    private final JLabel salesCountLabel = new JLabel("Продано товаров: 0");
    private final JLabel salesRevenueLabel = new JLabel("Выручка от продаж: 0,00 ₽");
    private final DefaultTableModel tableModel;

    public ReportPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        fromField.setToolTipText("Формат: ГГГГ-ММ-ДД");
        toField.setToolTipText("Формат: ГГГГ-ММ-ДД");

        JPanel top = new JPanel(new BorderLayout(4, 4));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        filters.add(new JLabel("Период с (ГГГГ-ММ-ДД):"));
        filters.add(fromField);
        filters.add(new JLabel("по (ГГГГ-ММ-ДД):"));
        filters.add(toField);
        filters.add(buildButton);
        top.add(filters, BorderLayout.NORTH);

        JPanel summary = new JPanel(new GridLayout(1, 2, 10, 0));
        summary.add(createSummaryBlock(
                "Договоры за период",
                contractCountLabel,
                totalLoansLabel,
                totalCommissionLabel
        ));
        summary.add(createSummaryBlock(
                "Продажи за период",
                salesCountLabel,
                salesRevenueLabel
        ));
        top.add(summary, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{
                "ID договора", "Клиент", "Дата", "Срок", "Выдано",
                "Комиссия", "Оценка залога", "Товаров"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                super.setValue(value instanceof BigDecimal
                        ? formatMoney((BigDecimal) value) : value);
            }
        };
        for (int column = 4; column <= 6; column++) {
            table.getColumnModel().getColumn(column).setCellRenderer(moneyRenderer);
        }
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JPanel createSummaryBlock(String title, JLabel... labels) {
        JPanel block = new JPanel(new GridLayout(labels.length, 1, 0, 3));
        block.setBorder(BorderFactory.createTitledBorder(title));
        for (JLabel label : labels) {
            block.add(label);
        }
        return block;
    }

    public void setReportSummary(int rowCount, BigDecimal totalLoans,
                                 BigDecimal totalCommission, int saleCount,
                                 BigDecimal salesRevenue) {
        contractCountLabel.setText("Договоров: " + rowCount);
        totalLoansLabel.setText("Выдано: " + formatMoney(totalLoans));
        totalCommissionLabel.setText(
                "Начисленная комиссия: " + formatMoney(totalCommission));
        salesCountLabel.setText("Продано товаров: " + saleCount);
        salesRevenueLabel.setText("Выручка от продаж: " + formatMoney(salesRevenue));
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag("ru-RU"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(value == null ? BigDecimal.ZERO : value) + " ₽";
    }

    public JTextField getFromField() { return fromField; }
    public JTextField getToField() { return toField; }
    public JButton getBuildButton() { return buildButton; }
    public DefaultTableModel getTableModel() { return tableModel; }
}
