package ru.mospolytech.pawnshop.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Singleton: в течение сеанса приложение использует одно подключение. */
public final class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private final Properties properties = new Properties();

    private DatabaseConnection() {
        loadProperties();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBC-драйвер MySQL не найден", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    properties.getProperty("db.url"),
                    properties.getProperty("db.user"),
                    properties.getProperty("db.password")
            );
        }
        return connection;
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private void loadProperties() {
        try (InputStream input = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
            if (input == null) {
                throw new IllegalStateException("В ресурсах не найден db.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать db.properties", e);
        }

        Path externalConfig = Path.of("db.properties");
        if (Files.exists(externalConfig)) {
            try (InputStream input = Files.newInputStream(externalConfig)) {
                properties.load(input);
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось прочитать внешний db.properties", e);
            }
        }
    }
}
