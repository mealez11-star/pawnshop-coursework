package ru.mospolytech.pawnshop.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class ReportPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTextField fromField = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
    private final JTextField toField = new JTextField(LocalDate.now().toString(), 10);
    private final JButton buildButton = new JButton("Построить отчёт");
    private final JLabel totalLoansLabel = new JLabel("Выдано: 0.00");
    private final JLabel totalCommissionLabel = new JLabel("Комиссия: 0.00");
    private final DefaultTableModel tableModel;

    public ReportPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        fromField.setToolTipText("Формат: ГГГГ-ММ-ДД");
        toField.setToolTipText("Формат: ГГГГ-ММ-ДД");

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("Период с (ГГГГ-ММ-ДД):"));
        filters.add(fromField);
        filters.add(new JLabel("по (ГГГГ-ММ-ДД):"));
        filters.add(toField);
        filters.add(buildButton);
        filters.add(Box.createHorizontalStrut(20));
        filters.add(totalLoansLabel);
        filters.add(Box.createHorizontalStrut(10));
        filters.add(totalCommissionLabel);
        add(filters, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{
                "ID договора", "Клиент", "Дата", "Срок", "Выдано", "Комиссия", "Оценка залога", "Товаров"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public JTextField getFromField() { return fromField; }
    public JTextField getToField() { return toField; }
    public JButton getBuildButton() { return buildButton; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JLabel getTotalLoansLabel() { return totalLoansLabel; }
    public JLabel getTotalCommissionLabel() { return totalCommissionLabel; }
}
