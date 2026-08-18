package ru.mospolytech.pawnshop.view;

import ru.mospolytech.pawnshop.model.Item;

import javax.swing.*;
import java.time.LocalDate;

public class PricesPanel extends BaseTablePanel {
    private static final long serialVersionUID = 1L;
    private final JComboBox<Item> itemCombo = new JComboBox<>();
    private final JTextField dateField = new JTextField(12);
    private final JTextField valueField = new JTextField(12);

    public PricesPanel() {
        super(new String[]{"ID", "Товар", "Дата", "Цена"});
        dateField.setText(LocalDate.now().toString());
        dateField.setToolTipText("Формат: ГГГГ-ММ-ДД");
        addFormRow("Товар:", itemCombo);
        addFormRow("Дата цены (ГГГГ-ММ-ДД):", dateField);
        addFormRow("Цена:", valueField);
    }

    public JComboBox<Item> getItemCombo() { return itemCombo; }
    public JTextField getDateField() { return dateField; }
    public JTextField getValueField() { return valueField; }
}
