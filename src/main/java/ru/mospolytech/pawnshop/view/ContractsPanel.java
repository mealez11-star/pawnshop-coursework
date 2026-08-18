package ru.mospolytech.pawnshop.view;

import ru.mospolytech.pawnshop.model.Client;

import javax.swing.*;
import java.time.LocalDate;

public class ContractsPanel extends BaseTablePanel {
    private static final long serialVersionUID = 1L;
    private final JComboBox<Client> clientCombo = new JComboBox<>();
    private final JTextField issueDateField = new JTextField(12);
    private final JTextField dueDateField = new JTextField(12);
    private final JTextField loanField = new JTextField(12);
    private final JTextField commissionField = new JTextField(12);
    private final JButton itemsButton = new JButton("Состав договора");

    public ContractsPanel() {
        super(new String[]{"ID", "Клиент", "Дата выдачи", "Срок возврата", "Сумма выдачи", "Комиссия"});
        LocalDate today = LocalDate.now();
        issueDateField.setText(today.toString());
        dueDateField.setText(today.plusDays(30).toString());
        issueDateField.setToolTipText("Формат: ГГГГ-ММ-ДД");
        dueDateField.setToolTipText("Формат: ГГГГ-ММ-ДД");
        addFormRow("Клиент:", clientCombo);
        addFormRow("Дата выдачи (ГГГГ-ММ-ДД):", issueDateField);
        addFormRow("Срок возврата (ГГГГ-ММ-ДД):", dueDateField);
        addFormRow("Сумма выдачи:", loanField);
        addFormRow("Комиссия:", commissionField);
        getButtonPanel().add(itemsButton);
    }

    public void setReadOnlyMode() {
        setFormVisible(false);
        getAddButton().setVisible(false);
        getUpdateButton().setVisible(false);
        getDeleteButton().setVisible(false);
    }

    public JComboBox<Client> getClientCombo() { return clientCombo; }
    public JTextField getIssueDateField() { return issueDateField; }
    public JTextField getDueDateField() { return dueDateField; }
    public JTextField getLoanField() { return loanField; }
    public JTextField getCommissionField() { return commissionField; }
    public JButton getItemsButton() { return itemsButton; }
}
