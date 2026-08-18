package ru.mospolytech.pawnshop.view;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final JTextField loginField = new JTextField(22);
    private final JTextField fullNameField = new JTextField(22);
    private final JTextField passportField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final JPasswordField repeatPasswordField = new JPasswordField(22);
    private final JButton registerButton = new JButton("Зарегистрироваться");
    private final JButton cancelButton = new JButton("Отмена");

    public RegisterDialog(JFrame owner) {
        super(owner, "Регистрация клиента", true);
        setSize(520, 360);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        addRow(panel, 0, "Логин:", loginField);
        addRow(panel, 1, "ФИО:", fullNameField);
        addRow(panel, 2, "Паспортные данные:", passportField);
        addRow(panel, 3, "Пароль:", passwordField);
        addRow(panel, 4, "Повтор пароля:", repeatPasswordField);

        JLabel hint = new JLabel("Не менее 8 символов: заглавная буква, цифра и спецсимвол");
        hint.setFont(hint.getFont().deriveFont(11f));
        GridBagConstraints hintConstraints = new GridBagConstraints();
        hintConstraints.gridx = 0;
        hintConstraints.gridy = 5;
        hintConstraints.gridwidth = 2;
        hintConstraints.insets = new Insets(5, 5, 10, 5);
        panel.add(hint, hintConstraints);

        JPanel buttons = new JPanel();
        buttons.add(registerButton);
        buttons.add(cancelButton);
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 6;
        buttonConstraints.gridwidth = 2;
        panel.add(buttons, buttonConstraints);

        setContentPane(panel);
        getRootPane().setDefaultButton(registerButton);
        cancelButton.addActionListener(event -> dispose());
    }

    private void addRow(JPanel panel, int row, String label, JComponent field) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = row;
        left.anchor = GridBagConstraints.WEST;
        left.insets = new Insets(5, 5, 5, 10);
        panel.add(new JLabel(label), left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = row;
        right.weightx = 1;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.insets = new Insets(5, 5, 5, 5);
        panel.add(field, right);
    }

    public JTextField getLoginField() { return loginField; }
    public JTextField getFullNameField() { return fullNameField; }
    public JTextField getPassportField() { return passportField; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JPasswordField getRepeatPasswordField() { return repeatPasswordField; }
    public JButton getRegisterButton() { return registerButton; }
}
