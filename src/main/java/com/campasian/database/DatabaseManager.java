package com.campasian.database;

import com.campasian.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Singleton manager for PostgreSQL connections.
 */
public final class DatabaseManager {

    private static volatile DatabaseManager instance;
    private final String jdbcUrl;
    private final String user;
    private final String password;

    private DatabaseManager() {
        this.jdbcUrl = withSslModeDisabled(DatabaseConfig.getJdbcUrl());
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
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC driver not found on the classpath/module-path.", e);
        }
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    public void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            // Connection successful
        }
    }

    private static String withSslModeDisabled(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }

        int queryStart = url.indexOf('?');
        if (queryStart < 0) {
            return url + "?sslmode=disable";
        }

        String base = url.substring(0, queryStart);
        String query = url.substring(queryStart + 1);

        Map<String, String> params = new LinkedHashMap<>();
        if (!query.isBlank()) {
            for (String pair : query.split("&")) {
                if (pair.isBlank()) continue;

                int eq = pair.indexOf('=');
                String key;
                String value;
                if (eq < 0) {
                    key = pair;
                    value = "";
                } else {
                    key = pair.substring(0, eq);
                    value = pair.substring(eq + 1);
                }
                params.put(key, value);
            }
        }

        // Ensure SSL is disabled regardless of how the URL was originally configured.
        params.remove("ssl");
        params.put("sslmode", "disable");

        StringBuilder rebuilt = new StringBuilder(base);
        if (!params.isEmpty()) {
            rebuilt.append('?');
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) rebuilt.append('&');
                first = false;

                rebuilt.append(entry.getKey());
                if (!entry.getValue().isEmpty()) {
                    rebuilt.append('=').append(entry.getValue());
                }
            }
        }

        return rebuilt.toString();
    }
}
