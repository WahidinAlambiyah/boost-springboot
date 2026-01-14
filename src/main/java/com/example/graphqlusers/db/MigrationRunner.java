package com.example.graphqlusers.db;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public final class MigrationRunner {
    private MigrationRunner() {
    }

    public static void migrate(DataSource dataSource) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
