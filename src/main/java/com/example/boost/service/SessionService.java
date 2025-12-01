package com.example.boost.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private static final String SESSION_KEY_PREFIX = "session:";
    private static final String USER_SESSION_KEY_PREFIX = "user_session:";

    private final RedisTemplate<String, Object> redisTemplate;

    public SessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeSession(String sessionId, UUID userId, Duration ttl) {
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        String userSessionKey = USER_SESSION_KEY_PREFIX + userId;

        Object existingSession = redisTemplate.opsForValue().get(userSessionKey);
        if (existingSession instanceof String existingSessionId && !existingSessionId.equals(sessionId)) {
            redisTemplate.delete(SESSION_KEY_PREFIX + existingSessionId);
        }

        redisTemplate.opsForValue().set(sessionKey, userId.toString(), ttl);
        redisTemplate.opsForValue().set(userSessionKey, sessionId, ttl);
    }

    public Optional<UUID> getUserIdForSession(String sessionId) {
        Object stored = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
        if (stored instanceof String userId) {
            return Optional.of(UUID.fromString(userId));
        }
        return Optional.empty();
    }

    public void invalidateSession(String sessionId) {
        getUserIdForSession(sessionId).ifPresent(userId -> {
            String userSessionKey = USER_SESSION_KEY_PREFIX + userId;
            Object currentSession = redisTemplate.opsForValue().get(userSessionKey);
            if (sessionId.equals(currentSession)) {
                redisTemplate.delete(userSessionKey);
            }
        });
        redisTemplate.delete(SESSION_KEY_PREFIX + sessionId);
    }

    public void invalidateUserSessions(UUID userId) {
        String userSessionKey = USER_SESSION_KEY_PREFIX + userId;
        Object sessionId = redisTemplate.opsForValue().get(userSessionKey);
        if (sessionId instanceof String existingSessionId) {
            redisTemplate.delete(SESSION_KEY_PREFIX + existingSessionId);
        }
        redisTemplate.delete(userSessionKey);
    }
}
