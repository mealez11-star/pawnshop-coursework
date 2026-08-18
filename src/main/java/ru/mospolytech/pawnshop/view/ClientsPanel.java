package ru.mospolytech.pawnshop.view;

import javax.swing.*;

public class ClientsPanel extends BaseTablePanel {
    private static final long serialVersionUID = 1L;
    private final JTextField fullNameField = new JTextField(25);
    private final JTextField passportField = new JTextField(25);

    public ClientsPanel() {
        super(new String[]{"ID клиента", "ФИО", "Паспорт", "ID пользователя"});
        addFormRow("ФИО:", fullNameField);
        addFormRow("Паспорт:", passportField);
        getAddButton().setVisible(false);
        getUpdateButton().setText("Изменить клиента");
    }

    public JTextField getFullNameField() { return fullNameField; }
    public JTextField getPassportField() { return passportField; }
}
