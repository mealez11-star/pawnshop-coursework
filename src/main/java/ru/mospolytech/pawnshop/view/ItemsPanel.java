package ru.mospolytech.pawnshop.view;

import ru.mospolytech.pawnshop.model.ItemStatus;

import javax.swing.*;

public class ItemsPanel extends BaseTablePanel {
    private static final long serialVersionUID = 1L;
    private final JTextField nameField = new JTextField(25);
    private final JComboBox<ItemStatus> statusCombo = new JComboBox<>(ItemStatus.values());

    public ItemsPanel() {
        super(new String[]{"ID", "Название", "Текущий статус"});
        addFormRow("Название:", nameField);
        addFormRow("Статус:", statusCombo);
    }

    public JTextField getNameField() { return nameField; }
    public JComboBox<ItemStatus> getStatusCombo() { return statusCombo; }
}
