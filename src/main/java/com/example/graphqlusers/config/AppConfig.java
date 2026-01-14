package com.example.graphqlusers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AppConfig {
    private final int appPort;
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String redisHost;
    private final int redisPort;
    private final boolean redisEnabled;
    private final String jwtSecret;
    private final Duration jwtTtl;
    private final Duration refreshTtl;

    public AppConfig(
            @Value("${APP_PORT:8080}") int appPort,
            @Value("${JDBC_URL:jdbc:postgresql://localhost:5432/appdb}") String jdbcUrl,
            @Value("${DB_USER:app}") String dbUser,
            @Value("${DB_PASSWORD:app}") String dbPassword,
            @Value("${REDIS_HOST:localhost}") String redisHost,
            @Value("${REDIS_PORT:6379}") int redisPort,
            @Value("${REDIS_ENABLED:true}") boolean redisEnabled,
            @Value("${JWT_SECRET:dev-secret-change-me-min-32-chars}") String jwtSecret,
            @Value("${JWT_TTL_MINUTES:15}") long jwtMinutes,
            @Value("${REFRESH_TTL_DAYS:30}") long refreshDays
    ) {
        this.appPort = appPort;
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisEnabled = redisEnabled;
        this.jwtSecret = jwtSecret;
        this.jwtTtl = Duration.ofMinutes(jwtMinutes);
        this.refreshTtl = Duration.ofDays(refreshDays);
    }

    public int appPort() {
        return appPort;
    }

    public String jdbcUrl() {
        return jdbcUrl;
    }

    public String dbUser() {
        return dbUser;
    }

    public String dbPassword() {
        return dbPassword;
    }

    public String redisHost() {
        return redisHost;
    }

    public int redisPort() {
        return redisPort;
    }

    public boolean redisEnabled() {
        return redisEnabled;
    }

    public String jwtSecret() {
        return jwtSecret;
    }

    public Duration jwtTtl() {
        return jwtTtl;
    }

    public Duration refreshTtl() {
        return refreshTtl;
    }
}
