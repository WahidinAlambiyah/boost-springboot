package com.alambiyah.userauth.framework.cache;

import com.alambiyah.userauth.domain.entity.User;
import com.alambiyah.userauth.dto.UserResponse;
import com.alambiyah.userauth.framework.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserCache {
    private static final String USER_PREFIX = "user:";

    private final RedisClient redisClient;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    @ConfigProperty(name = "app.cache.user-ttl")
    Duration userTtl;

    @Inject
    public UserCache(RedisClient redisClient, ObjectMapper objectMapper, UserMapper userMapper) {
        this.redisClient = redisClient;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
    }

    public Optional<UserResponse> get(UUID userId) {
        Response response = redisClient.get(key(userId));
        if (response == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(response.toString(), UserResponse.class));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    public void put(User user) {
        UserResponse response = userMapper.toResponse(user);
        try {
            String json = objectMapper.writeValueAsString(response);
            redisClient.setex(key(user.id), String.valueOf(userTtl.toSeconds()), json);
        } catch (JsonProcessingException ignored) {
        }
    }

    public void evict(UUID userId) {
        redisClient.del(key(userId));
    }

    private String key(UUID userId) {
        return USER_PREFIX + userId;
    }
}
