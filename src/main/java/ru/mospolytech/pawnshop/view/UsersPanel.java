package ru.mospolytech.pawnshop.view;

import ru.mospolytech.pawnshop.model.Role;

import javax.swing.*;

public class UsersPanel extends BaseTablePanel {
    private static final long serialVersionUID = 1L;
    private final JComboBox<Role> roleCombo = new JComboBox<>(Role.values());

    public UsersPanel() {
        super(new String[]{"ID", "Логин", "ФИО", "Роль"});
        addFormRow("Новая роль:", roleCombo);
        getAddButton().setVisible(false);
        getUpdateButton().setText("Изменить роль");
    }

    public JComboBox<Role> getRoleCombo() { return roleCombo; }
}
