package com.example.graphqlusers.config;

import java.time.Duration;

public record AppConfig(
        int appPort,
        String jdbcUrl,
        String dbUser,
        String dbPassword,
        String redisHost,
        int redisPort,
        String jwtSecret,
        Duration jwtTtl,
        Duration refreshTtl
) {
    public static AppConfig fromEnv() {
        int port = Integer.parseInt(envOrDefault("APP_PORT", "8080"));
        String jdbcUrl = envOrDefault("JDBC_URL", "jdbc:postgresql://localhost:5432/appdb");
        String dbUser = envOrDefault("DB_USER", "app");
        String dbPassword = envOrDefault("DB_PASSWORD", "app");
        String redisHost = envOrDefault("REDIS_HOST", "localhost");
        int redisPort = Integer.parseInt(envOrDefault("REDIS_PORT", "6379"));
        String jwtSecret = envOrDefault("JWT_SECRET", "dev-secret-change-me-min-32-chars");
        long jwtMinutes = Long.parseLong(envOrDefault("JWT_TTL_MINUTES", "15"));
        long refreshDays = Long.parseLong(envOrDefault("REFRESH_TTL_DAYS", "30"));
        return new AppConfig(
                port,
                jdbcUrl,
                dbUser,
                dbPassword,
                redisHost,
                redisPort,
                jwtSecret,
                Duration.ofMinutes(jwtMinutes),
                Duration.ofDays(refreshDays)
        );
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
