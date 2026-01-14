package com.example.boost.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final Map<String, StoredToken> inMemoryStore = new ConcurrentHashMap<>();
    private final Map<String, StoredToken> blacklistStore = new ConcurrentHashMap<>();

    @Value("${app.redis.enabled:true}")
    private boolean redisEnabled;

    public void storeRefreshToken(String userId, String tokenId, String token, Duration ttl) {
        String key = refreshKey(userId, tokenId);
        if (useRedis()) {
            redisTemplateProvider.getObject().opsForValue().set(key, token, ttl);
            return;
        }
        inMemoryStore.put(key, new StoredToken(token, Instant.now().plus(ttl)));
    }

    public boolean isRefreshTokenValid(String userId, String tokenId, String token) {
        String key = refreshKey(userId, tokenId);
        if (useRedis()) {
            String stored = redisTemplateProvider.getObject().opsForValue().get(key);
            return token.equals(stored);
        }
        return token.equals(readIfNotExpired(inMemoryStore, key));
    }

    public void revokeRefreshToken(String userId, String tokenId) {
        String key = refreshKey(userId, tokenId);
        if (useRedis()) {
            redisTemplateProvider.getObject().delete(key);
            return;
        }
        inMemoryStore.remove(key);
    }

    public void blacklistAccessToken(String jti, Date expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt.toInstant());
        if (!ttl.isNegative() && !ttl.isZero()) {
            if (useRedis()) {
                redisTemplateProvider.getObject().opsForValue().set(blacklistKey(jti), "1", ttl);
                return;
            }
            blacklistStore.put(blacklistKey(jti), new StoredToken("1", Instant.now().plus(ttl)));
        }
    }

    public boolean isAccessTokenBlacklisted(String jti) {
        String key = blacklistKey(jti);
        if (useRedis()) {
            return Boolean.TRUE.equals(redisTemplateProvider.getObject().hasKey(key));
        }
        return readIfNotExpired(blacklistStore, key) != null;
    }

    private boolean useRedis() {
        if (!redisEnabled) {
            return false;
        }
        StringRedisTemplate template = redisTemplateProvider.getIfAvailable();
        if (template == null) {
            log.warn("Redis is enabled but StringRedisTemplate is unavailable; falling back to in-memory store.");
            return false;
        }
        return true;
    }

    private String readIfNotExpired(Map<String, StoredToken> store, String key) {
        StoredToken stored = store.get(key);
        if (stored == null) {
            return null;
        }
        if (stored.expiresAt().isBefore(Instant.now())) {
            store.remove(key);
            return null;
        }
        return stored.value();
    }

    private String refreshKey(String userId, String tokenId) {
        return "refresh:" + userId + ":" + tokenId;
    }

    private String blacklistKey(String jti) {
        return "blacklist:" + jti;
    }

    private record StoredToken(String value, Instant expiresAt) {
    }
}
