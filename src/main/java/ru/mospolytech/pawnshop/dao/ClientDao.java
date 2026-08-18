package ru.mospolytech.pawnshop.dao;

import ru.mospolytech.pawnshop.config.DatabaseConnection;
import ru.mospolytech.pawnshop.model.Client;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDao {
    private final DatabaseConnection database = DatabaseConnection.getInstance();

    public List<Client> findAll() throws SQLException {
        String sql = "SELECT id_client, full_name, passport_data, id_user FROM clients ORDER BY id_client";
        List<Client> clients = new ArrayList<>();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                clients.add(map(result));
            }
        }
        return clients;
    }

    public Optional<Client> findByUserId(int userId) throws SQLException {
        String sql = "SELECT id_client, full_name, passport_data, id_user FROM clients WHERE id_user = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public void update(Client client) throws SQLException {
        String clientSql = "UPDATE clients SET full_name = ?, passport_data = ? WHERE id_client = ?";
        String userSql = "UPDATE users SET full_name = ? WHERE id_user = ?";
        Connection connection = database.getConnection();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            try (PreparedStatement statement = connection.prepareStatement(clientSql)) {
                statement.setString(1, client.getFullName());
                statement.setString(2, client.getPassportData());
                statement.setInt(3, client.getId());
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(userSql)) {
                statement.setString(1, client.getFullName());
                statement.setInt(2, client.getUserId());
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

    public void delete(int clientId) throws SQLException {
        String sql = "DELETE FROM clients WHERE id_client = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clientId);
            statement.executeUpdate();
        }
    }

    private Client map(ResultSet result) throws SQLException {
        return new Client(
                result.getInt("id_client"),
                result.getString("full_name"),
                result.getString("passport_data"),
                result.getInt("id_user")
        );
    }
}
