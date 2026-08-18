package ru.mospolytech.pawnshop.view;

import ru.mospolytech.pawnshop.model.Item;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ContractItemsDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<Item> itemCombo = new JComboBox<>();
    private final JTextField assessedValueField = new JTextField(12);
    private final JButton addButton = new JButton("Добавить товар");
    private final JButton updateButton = new JButton("Изменить оценку");
    private final JButton removeButton = new JButton("Убрать товар");

    public ContractItemsDialog(JFrame owner, int contractId, boolean editable) {
        super(owner, "Состав договора № " + contractId, true);
        setSize(620, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        tableModel = new DefaultTableModel(new String[]{"ID товара", "Название", "Оценочная стоимость"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (editable) {
            controls.add(new JLabel("Товар:"));
            controls.add(itemCombo);
            controls.add(new JLabel("Оценка:"));
            controls.add(assessedValueField);
            controls.add(addButton);
            controls.add(updateButton);
            controls.add(removeButton);
        }
        JButton closeButton = new JButton("Закрыть");
        controls.add(closeButton);
        closeButton.addActionListener(event -> dispose());
        add(controls, BorderLayout.SOUTH);

    }

    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getTable() { return table; }
    public JComboBox<Item> getItemCombo() { return itemCombo; }
    public JTextField getAssessedValueField() { return assessedValueField; }
    public JButton getAddButton() { return addButton; }
    public JButton getUpdateButton() { return updateButton; }
    public JButton getRemoveButton() { return removeButton; }
}
