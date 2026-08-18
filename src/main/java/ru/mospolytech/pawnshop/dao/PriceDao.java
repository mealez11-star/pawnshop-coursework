package ru.mospolytech.pawnshop.dao;

import ru.mospolytech.pawnshop.config.DatabaseConnection;
import ru.mospolytech.pawnshop.model.Price;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PriceDao {
    private final DatabaseConnection database = DatabaseConnection.getInstance();

    public List<Price> findAll() throws SQLException {
        String sql = "SELECT p.id_price, p.price_date, p.value, p.id_item, i.name AS item_name "
                + "FROM prices p JOIN items i ON i.id_item = p.id_item "
                + "ORDER BY p.id_price";
        List<Price> prices = new ArrayList<>();
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                prices.add(map(result));
            }
        }
        return prices;
    }

    public int create(Price price) throws SQLException {
        String sql = "INSERT INTO prices(price_date, value, id_item) VALUES (?, ?, ?)";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setFields(statement, price);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("СУБД не вернула идентификатор цены");
                }
                return keys.getInt(1);
            }
        }
    }

    public void update(Price price) throws SQLException {
        ensureNotUsedInSale(price.getId());
        String sql = "UPDATE prices SET price_date = ?, value = ?, id_item = ? WHERE id_price = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            setFields(statement, price);
            statement.setInt(4, price.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int priceId) throws SQLException {
        ensureNotUsedInSale(priceId);
        String sql = "DELETE FROM prices WHERE id_price = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setInt(1, priceId);
            statement.executeUpdate();
        }
    }

    private void ensureNotUsedInSale(int priceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sales WHERE id_price = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setInt(1, priceId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) > 0) {
                    throw new SQLException(
                            "Цена уже использована в продаже и должна остаться неизменной"
                    );
                }
            }
        }
    }

    private void setFields(PreparedStatement statement, Price price) throws SQLException {
        statement.setDate(1, Date.valueOf(price.getDate()));
        statement.setBigDecimal(2, price.getValue());
        statement.setInt(3, price.getItemId());
    }

    private Price map(ResultSet result) throws SQLException {
        return new Price(
                result.getInt("id_price"),
                result.getDate("price_date").toLocalDate(),
                result.getBigDecimal("value"),
                result.getInt("id_item"),
                result.getString("item_name")
        );
    }
}
