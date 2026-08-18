package ru.mospolytech.pawnshop.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BaseTablePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JPanel formPanel = new JPanel(new GridBagLayout());
    private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JButton addButton = new JButton("Добавить");
    private final JButton updateButton = new JButton("Изменить");
    private final JButton deleteButton = new JButton("Удалить");
    private final JButton refreshButton = new JButton("Обновить");
    private int formRow;

    public BaseTablePanel(String[] columns) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(formPanel, BorderLayout.CENTER);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        bottom.add(buttonPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
    }

    protected void addFormRow(String label, JComponent component) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = formRow;
        left.anchor = GridBagConstraints.WEST;
        left.insets = new Insets(3, 3, 3, 8);
        formPanel.add(new JLabel(label), left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = formRow++;
        right.weightx = 1;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.insets = new Insets(3, 3, 3, 3);
        formPanel.add(component, right);
    }

    protected JPanel getButtonPanel() { return buttonPanel; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
    public JButton getAddButton() { return addButton; }
    public JButton getUpdateButton() { return updateButton; }
    public JButton getDeleteButton() { return deleteButton; }
    public JButton getRefreshButton() { return refreshButton; }
    public void setFormVisible(boolean visible) { formPanel.setVisible(visible); }

    public int getSelectedModelRow() {
        int viewRow = table.getSelectedRow();
        return viewRow < 0 ? -1 : table.convertRowIndexToModel(viewRow);
    }

    public void setRows(Object[][] rows) {
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
    }
}
