package ru.mospolytech.pawnshop.view;

import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JTextField loginField = new JTextField(25);
    private final JTextField fullNameField = new JTextField(25);
    private final JTextField passportField = new JTextField(25);
    private final JPasswordField passwordField = new JPasswordField(25);
    private final JButton saveButton = new JButton("Сохранить изменения");

    public ProfilePanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        addRow(0, "Логин:", loginField);
        addRow(1, "ФИО:", fullNameField);
        addRow(2, "Паспортные данные:", passportField);
        addRow(3, "Новый пароль (необязательно):", passwordField);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(12, 5, 5, 5);
        add(saveButton, constraints);

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 5;
        filler.weighty = 1;
        add(Box.createVerticalGlue(), filler);
    }

    private void addRow(int row, String label, JComponent field) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = row;
        left.anchor = GridBagConstraints.WEST;
        left.insets = new Insets(5, 5, 5, 10);
        add(new JLabel(label), left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = row;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.weightx = 1;
        right.insets = new Insets(5, 5, 5, 5);
        add(field, right);
    }

    public JTextField getLoginField() { return loginField; }
    public JTextField getFullNameField() { return fullNameField; }
    public JTextField getPassportField() { return passportField; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JButton getSaveButton() { return saveButton; }

    public void setPassportEnabled(boolean enabled) {
        passportField.setEnabled(enabled);
        if (!enabled) {
            passportField.setText("Не применяется для администратора");
        }
    }
}
