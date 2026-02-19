package com.campasian.database;

import com.campasian.config.DatabaseConfig;
import org.flywaydb.core.Flyway;

/**
 * Runs Flyway migrations at application startup.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {}

    public static void migrate() {
        Flyway flyway = Flyway.configure()
            .dataSource(
                DatabaseConfig.getJdbcUrl(),
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD
            )
            .locations("classpath:db/migration")
            .load();
        flyway.migrate();
    }
}
