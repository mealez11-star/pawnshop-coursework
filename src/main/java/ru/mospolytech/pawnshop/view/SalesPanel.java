package ru.mospolytech.pawnshop.view;

import ru.mospolytech.pawnshop.model.Item;
import ru.mospolytech.pawnshop.model.Price;

import javax.swing.*;
import java.time.LocalDate;

public class SalesPanel extends BaseTablePanel {
    private static final long serialVersionUID = 1L;
    private final JTextField dateField = new JTextField(12);
    private final JComboBox<Item> itemCombo = new JComboBox<>();
    private final JComboBox<Price> priceCombo = new JComboBox<>();

    public SalesPanel() {
        super(new String[]{"ID", "Дата продажи", "Товар", "ID цены", "Цена продажи"});
        dateField.setText(LocalDate.now().toString());
        dateField.setToolTipText("Формат: ГГГГ-ММ-ДД");
        addFormRow("Дата продажи (ГГГГ-ММ-ДД):", dateField);
        addFormRow("Товар:", itemCombo);
        addFormRow("Цена:", priceCombo);
    }

    public JTextField getDateField() { return dateField; }
    public JComboBox<Item> getItemCombo() { return itemCombo; }
    public JComboBox<Price> getPriceCombo() { return priceCombo; }
}
