package com.example.boost.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final StringRedisTemplate redisTemplate;

    public void storeRefreshToken(String userId, String tokenId, String token, Duration ttl) {
        String key = refreshKey(userId, tokenId);
        redisTemplate.opsForValue().set(key, token, ttl);
    }

    public boolean isRefreshTokenValid(String userId, String tokenId, String token) {
        String key = refreshKey(userId, tokenId);
        String stored = redisTemplate.opsForValue().get(key);
        return token.equals(stored);
    }

    public void revokeRefreshToken(String userId, String tokenId) {
        redisTemplate.delete(refreshKey(userId, tokenId));
    }

    public void blacklistAccessToken(String jti, Date expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt.toInstant());
        if (!ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.opsForValue().set(blacklistKey(jti), "1", ttl);
        }
    }

    public boolean isAccessTokenBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey(jti)));
    }

    private String refreshKey(String userId, String tokenId) {
        return "refresh:" + userId + ":" + tokenId;
    }

    private String blacklistKey(String jti) {
        return "blacklist:" + jti;
    }
}
