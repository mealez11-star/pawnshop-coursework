package ru.mospolytech.pawnshop.controller;

import ru.mospolytech.pawnshop.dao.UserDao;
import ru.mospolytech.pawnshop.model.User;
import ru.mospolytech.pawnshop.util.PasswordUtils;
import ru.mospolytech.pawnshop.util.ValidationUtils;
import ru.mospolytech.pawnshop.view.LoginView;
import ru.mospolytech.pawnshop.view.MainView;
import ru.mospolytech.pawnshop.view.RegisterDialog;
import ru.mospolytech.pawnshop.view.ViewUtils;

import java.sql.SQLException;

public class AuthController {
    private final UserDao userDao = new UserDao();
    private final LoginView loginView = new LoginView();

    public AuthController() {
        loginView.getLoginButton().addActionListener(event -> login());
        loginView.getRegisterButton().addActionListener(event -> openRegistration());
    }

    public void start() {
        try {
            if (userDao.createDefaultAdminIfNoUsers()) {
                ViewUtils.showInfo(loginView,
                        "Создан первый администратор.\nЛогин: admin\nПароль: Admin@123\n"
                                + "После входа смените пароль в профиле.");
            }
        } catch (SQLException e) {
            ViewUtils.showError(loginView,
                    "Нет подключения к базе данных.\n"
                            + "1. Выполните database/schema.sql в MySQL.\n"
                            + "2. Проверьте настройки db.properties.\n\n"
                            + shortSqlMessage(e));
        }
        loginView.setVisible(true);
    }

    private void login() {
        try {
            String login = ValidationUtils.requireText(loginView.getLoginField().getText(), "Логин");
            String password = new String(loginView.getPasswordField().getPassword());
            User user = userDao.findByLogin(login)
                    .filter(found -> PasswordUtils.matches(password, found.getPasswordHash()))
                    .orElseThrow(() -> new IllegalArgumentException("Неверный логин или пароль"));

            loginView.setVisible(false);
            MainView mainView = new MainView(user);
            new MainController(user, mainView, this::returnToLogin);
            mainView.setVisible(true);
        } catch (IllegalArgumentException e) {
            ViewUtils.showError(loginView, e.getMessage());
        } catch (SQLException e) {
            ViewUtils.showError(loginView, shortSqlMessage(e));
        }
    }

    private void openRegistration() {
        RegisterDialog dialog = new RegisterDialog(loginView);
        dialog.getRegisterButton().addActionListener(event -> register(dialog));
        dialog.setVisible(true);
    }

    private void register(RegisterDialog dialog) {
        try {
            String login = ValidationUtils.requireText(dialog.getLoginField().getText(), "Логин");
            String fullName = ValidationUtils.requireText(dialog.getFullNameField().getText(), "ФИО");
            String passport = ValidationUtils.requireText(dialog.getPassportField().getText(), "Паспортные данные");
            String password = new String(dialog.getPasswordField().getPassword());
            String repeated = new String(dialog.getRepeatPasswordField().getPassword());

            if (!password.equals(repeated)) {
                throw new IllegalArgumentException("Пароли не совпадают");
            }
            if (!PasswordUtils.isStrong(password)) {
                throw new IllegalArgumentException(
                        "Пароль должен содержать не менее 8 символов, заглавную букву, цифру и специальный символ"
                );
            }

            userDao.createRegularUser(login, PasswordUtils.hash(password), fullName, passport);
            ViewUtils.showInfo(dialog, "Регистрация завершена. Теперь можно войти.");
            loginView.getLoginField().setText(login);
            dialog.dispose();
        } catch (IllegalArgumentException e) {
            ViewUtils.showError(dialog, e.getMessage());
        } catch (SQLException e) {
            ViewUtils.showError(dialog, shortSqlMessage(e));
        }
    }

    private void returnToLogin() {
        loginView.getPasswordField().setText("");
        loginView.setVisible(true);
    }

    private String shortSqlMessage(SQLException e) {
        if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
            return "Нарушено ограничение целостности. Возможно, логин или паспорт уже заняты.\n"
                    + e.getMessage();
        }
        return "Ошибка работы с базой данных:\n" + e.getMessage();
    }
}
