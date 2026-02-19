package com.campasian.database;

import com.campasian.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton manager for PostgreSQL connections.
 */
public final class DatabaseManager {

    private static volatile DatabaseManager instance;
    private final String jdbcUrl;
    private final String user;
    private final String password;

    private DatabaseManager() {
        this.jdbcUrl = DatabaseConfig.getJdbcUrl();
        this.user = DatabaseConfig.USER;
        this.password = DatabaseConfig.PASSWORD;
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Returns a new connection. Caller must close it.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    public void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            // Connection successful
        }
    }
}
