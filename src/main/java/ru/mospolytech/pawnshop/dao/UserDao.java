package ru.mospolytech.pawnshop.dao;

import ru.mospolytech.pawnshop.config.DatabaseConnection;
import ru.mospolytech.pawnshop.model.Role;
import ru.mospolytech.pawnshop.model.User;
import ru.mospolytech.pawnshop.util.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private final DatabaseConnection database = DatabaseConnection.getInstance();

    public Optional<User> findByLogin(String login) throws SQLException {
        String sql = "SELECT id_user, login, password_hash, full_name, role FROM users WHERE login = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT id_user, login, password_hash, full_name, role FROM users ORDER BY id_user";
        List<User> users = new ArrayList<>();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                users.add(map(result));
            }
        }
        return users;
    }

    /** Регистрация пользователя и клиента выполняется одной транзакцией. */
    public User createRegularUser(String login, String passwordHash,
                                  String fullName, String passportData) throws SQLException {
        String userSql = "INSERT INTO users(login, password_hash, full_name, role) VALUES (?, ?, ?, 'USER')";
        String clientSql = "INSERT INTO clients(full_name, passport_data, id_user) VALUES (?, ?, ?)";
        Connection connection = database.getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            int userId;
            try (PreparedStatement statement = connection.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, login);
                statement.setString(2, passwordHash);
                statement.setString(3, fullName);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("СУБД не вернула идентификатор пользователя");
                    }
                    userId = keys.getInt(1);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(clientSql)) {
                statement.setString(1, fullName);
                statement.setString(2, passportData);
                statement.setInt(3, userId);
                statement.executeUpdate();
            }

            connection.commit();
            return new User(userId, login, passwordHash, fullName, Role.USER);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public boolean createDefaultAdminIfNoUsers() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement statement = database.getConnection().prepareStatement(countSql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            if (result.getInt(1) > 0) {
                return false;
            }
        }

        String insertSql = "INSERT INTO users(login, password_hash, full_name, role) VALUES (?, ?, ?, 'ADMIN')";
        try (PreparedStatement statement = database.getConnection().prepareStatement(insertSql)) {
            statement.setString(1, "admin");
            statement.setString(2, PasswordUtils.hash("Admin@123"));
            statement.setString(3, "Администратор");
            statement.executeUpdate();
            return true;
        }
    }

    /** Изменение профиля также синхронно меняет ФИО и паспорт клиента. */
    public void updateProfile(int userId, String login, String fullName,
                              String passportData, String newPasswordHash) throws SQLException {
        String userSql = newPasswordHash == null
                ? "UPDATE users SET login = ?, full_name = ? WHERE id_user = ?"
                : "UPDATE users SET login = ?, full_name = ?, password_hash = ? WHERE id_user = ?";
        String clientSql = "UPDATE clients SET full_name = ?, passport_data = ? WHERE id_user = ?";
        Connection connection = database.getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement statement = connection.prepareStatement(userSql)) {
                statement.setString(1, login);
                statement.setString(2, fullName);
                if (newPasswordHash == null) {
                    statement.setInt(3, userId);
                } else {
                    statement.setString(3, newPasswordHash);
                    statement.setInt(4, userId);
                }
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(clientSql)) {
                statement.setString(1, fullName);
                statement.setString(2, passportData);
                statement.setInt(3, userId);
                statement.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public void updateRole(int userId, Role role) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE id_user = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, role.name());
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    public void delete(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id_user = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    private User map(ResultSet result) throws SQLException {
        return new User(
                result.getInt("id_user"),
                result.getString("login"),
                result.getString("password_hash"),
                result.getString("full_name"),
                Role.valueOf(result.getString("role"))
        );
    }
}
