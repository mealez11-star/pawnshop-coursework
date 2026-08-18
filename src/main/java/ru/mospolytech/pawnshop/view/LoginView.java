package ru.mospolytech.pawnshop.view;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    private static final long serialVersionUID = 1L;
    private final JTextField loginField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginButton = new JButton("Войти");
    private final JButton registerButton = new JButton("Регистрация");

    public LoginView() {
        super("Ломбард - вход");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(6, 6, 6, 6);

        JLabel title = new JLabel("Информационная система ломбарда", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 17f));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        panel.add(title, constraints);

        constraints.gridwidth = 1;
        constraints.gridy = 1;
        panel.add(new JLabel("Логин:"), constraints);
        constraints.gridx = 1;
        panel.add(loginField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(new JLabel("Пароль:"), constraints);
        constraints.gridx = 1;
        panel.add(passwordField, constraints);

        JPanel buttons = new JPanel();
        buttons.add(loginButton);
        buttons.add(registerButton);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        panel.add(buttons, constraints);

        setContentPane(panel);
        getRootPane().setDefaultButton(loginButton);
    }

    public JTextField getLoginField() { return loginField; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JButton getLoginButton() { return loginButton; }
    public JButton getRegisterButton() { return registerButton; }
}
