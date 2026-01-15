package com.example.graphqlusers.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public class RedisRefreshTokenStore implements RefreshTokenStore {
    private final JedisPool jedisPool;
    private final Duration ttl;

    public RedisRefreshTokenStore(JedisPool jedisPool, Duration ttl) {
        this.jedisPool = jedisPool;
        this.ttl = ttl;
    }

    @Override
    public String issue(UUID userId) {
        String token = UUID.randomUUID().toString();
        String key = key(token);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, ttl.toSeconds(), userId.toString());
        }
        return token;
    }

    @Override
    public Optional<UUID> getUserId(String token) {
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(key(token));
            return value == null ? Optional.empty() : Optional.of(UUID.fromString(value));
        }
    }

    @Override
    public void revoke(String token) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key(token));
        }
    }

    private String key(String token) {
        return "refresh:" + token;
    }
}
