package ru.mospolytech.pawnshop.dao;

import ru.mospolytech.pawnshop.config.DatabaseConnection;
import ru.mospolytech.pawnshop.model.Item;
import ru.mospolytech.pawnshop.model.ItemStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ItemDao {
    private final DatabaseConnection database = DatabaseConnection.getInstance();

    public List<Item> findAll() throws SQLException {
        String sql = "SELECT id_item, name, current_status FROM items ORDER BY id_item";
        List<Item> items = new ArrayList<>();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                items.add(map(result));
            }
        }
        return items;
    }

    public int create(Item item) throws SQLException {
        String sql = "INSERT INTO items(name, current_status) VALUES (?, ?)";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, item.getName());
            statement.setString(2, item.getStatus().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("СУБД не вернула идентификатор товара");
                }
                return keys.getInt(1);
            }
        }
    }

    public void update(Item item) throws SQLException {
        String sql = "UPDATE items SET name = ?, current_status = ? WHERE id_item = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, item.getName());
            statement.setString(2, item.getStatus().name());
            statement.setInt(3, item.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int itemId) throws SQLException {
        String sql = "DELETE FROM items WHERE id_item = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setInt(1, itemId);
            statement.executeUpdate();
        }
    }

    private Item map(ResultSet result) throws SQLException {
        return new Item(
                result.getInt("id_item"),
                result.getString("name"),
                ItemStatus.valueOf(result.getString("current_status"))
        );
    }
}
