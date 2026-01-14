package com.example.graphqlusers.config;

import com.example.graphqlusers.auth.JwtService;
import com.example.graphqlusers.db.DataSourceFactory;
import com.example.graphqlusers.redis.InMemoryRefreshTokenStore;
import com.example.graphqlusers.redis.RedisFactory;
import com.example.graphqlusers.redis.RedisRefreshTokenStore;
import com.example.graphqlusers.redis.RefreshTokenStore;
import com.example.graphqlusers.repository.PostgresUserRepository;
import com.example.graphqlusers.repository.UserRepository;
import com.example.graphqlusers.service.AuthService;
import com.example.graphqlusers.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

import javax.sql.DataSource;

@Configuration
public class AppConfiguration {
    @Bean
    public DataSource dataSource(AppConfig config) {
        return DataSourceFactory.create(config.jdbcUrl(), config.dbUser(), config.dbPassword());
    }

    @Bean
    @ConditionalOnProperty(name = "REDIS_ENABLED", havingValue = "true", matchIfMissing = true)
    public JedisPool jedisPool(AppConfig config) {
        return RedisFactory.create(config.redisHost(), config.redisPort());
    }

    @Bean
    @ConditionalOnProperty(name = "REDIS_ENABLED", havingValue = "true", matchIfMissing = true)
    public RefreshTokenStore redisRefreshTokenStore(JedisPool jedisPool, AppConfig config) {
        return new RedisRefreshTokenStore(jedisPool, config.refreshTtl());
    }

    @Bean
    @ConditionalOnProperty(name = "REDIS_ENABLED", havingValue = "false")
    public RefreshTokenStore inMemoryRefreshTokenStore(AppConfig config) {
        return new InMemoryRefreshTokenStore(config.refreshTtl());
    }

    @Bean
    public JwtService jwtService(AppConfig config) {
        return new JwtService(config.jwtSecret(), config.jwtTtl());
    }

    @Bean
    public UserRepository userRepository(DataSource dataSource) {
        return new PostgresUserRepository(dataSource);
    }

    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserService(userRepository);
    }

    @Bean
    public AuthService authService(UserRepository userRepository, JwtService jwtService, RefreshTokenStore refreshTokenStore) {
        return new AuthService(userRepository, jwtService, refreshTokenStore);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
