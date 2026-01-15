package com.example.graphqlusers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AppConfig {
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
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.datasource.username}") String dbUser,
            @Value("${spring.datasource.password}") String dbPassword,
            @Value("${spring.data.redis.host:localhost}") String redisHost,
            @Value("${spring.data.redis.port:6379}") int redisPort,
            @Value("${app.redis.enabled:true}") boolean redisEnabled,
            @Value("${security.jwt.secret}") String jwtSecret,
            @Value("${security.jwt.access-token-ttl-minutes:15}") long jwtMinutes,
            @Value("${security.jwt.refresh-token-ttl-days:30}") long refreshDays
    ) {
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
