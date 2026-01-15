package com.example.graphqlusers.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class DataSourceFactory {
    private DataSourceFactory() {
    }

    public static DataSource create(String jdbcUrl, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setPoolName("app-hikari");
        return new HikariDataSource(config);
    }
}
