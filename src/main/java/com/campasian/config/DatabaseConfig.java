package com.campasian.config;

/**
 * Database connection configuration for Supabase (PostgreSQL).
 * For production, use environment variables or external config.
 */
public final class DatabaseConfig {

    private DatabaseConfig() {}

    public static final String HOST = "db.ixcmrdmkdlqtnhefjnsx.supabase.co";
    public static final int PORT = 5432;
    public static final String DATABASE = "postgres";
    public static final String USER = "postgres";
    public static final String PASSWORD = "abirabdullah3491";

    public static String getJdbcUrl() {
        return String.format(
                "jdbc:postgresql://db.ixcmrdmkdlqtnhefjnsx.supabase.co:6543/postgres?sslmode=disable"
        );
    }
}
