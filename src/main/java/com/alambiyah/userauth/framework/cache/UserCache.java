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
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class UserCache {
    private static final String USER_PREFIX = "user:";

    private final RedisClient redisClient;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    @ConfigProperty(name = "app.cache.user-ttl")
    Duration userTtl;

    @ConfigProperty(name = "app.redis.enabled", defaultValue = "true")
    boolean redisEnabled;

    private final Map<UUID, CacheEntry> inMemoryCache = new ConcurrentHashMap<>();

    @Inject
    public UserCache(RedisClient redisClient, ObjectMapper objectMapper, UserMapper userMapper) {
        this.redisClient = redisClient;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
    }

    public Optional<UserResponse> get(UUID userId) {
        if (!redisEnabled) {
            CacheEntry entry = inMemoryCache.get(userId);
            if (entry == null || entry.isExpired()) {
                inMemoryCache.remove(userId);
                return Optional.empty();
            }
            return Optional.of(entry.value());
        }
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
        if (!redisEnabled) {
            inMemoryCache.put(user.id, new CacheEntry(response, Instant.now().plus(userTtl)));
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(response);
            redisClient.setex(key(user.id), String.valueOf(userTtl.toSeconds()), json);
        } catch (JsonProcessingException ignored) {
        }
    }

    public void evict(UUID userId) {
        if (redisEnabled) {
            redisClient.del(List.of(key(userId)));
        } else {
            inMemoryCache.remove(userId);
        }
    }

    private String key(UUID userId) {
        return USER_PREFIX + userId;
    }

    private record CacheEntry(UserResponse value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
