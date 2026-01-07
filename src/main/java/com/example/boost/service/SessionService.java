package com.example.boost.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class SessionService {

    private static final String SESSION_KEY_PREFIX = "session:";
    private static final String USER_SESSION_KEY_PREFIX = "user_session:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final boolean redisEnabled;
    private final Map<String, SessionRecord> inMemorySessions = new ConcurrentHashMap<>();
    private final Map<UUID, UserSessionRecord> inMemoryUserSessions = new ConcurrentHashMap<>();

    public SessionService(ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider,
                          @Value("${app.redis.enabled:true}") boolean redisEnabled) {
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        this.redisEnabled = redisEnabled && this.redisTemplate != null;
    }

    public void storeSession(String sessionId, UUID userId, Duration ttl) {
        if (!redisEnabled) {
            storeSessionInMemory(sessionId, userId, ttl);
            return;
        }
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
        if (!redisEnabled) {
            return getUserIdForSessionInMemory(sessionId);
        }
        Object stored = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + sessionId);
        if (stored instanceof String userId) {
            return Optional.of(UUID.fromString(userId));
        }
        return Optional.empty();
    }

    public void invalidateSession(String sessionId) {
        if (!redisEnabled) {
            invalidateSessionInMemory(sessionId);
            return;
        }
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
        if (!redisEnabled) {
            invalidateUserSessionsInMemory(userId);
            return;
        }
        String userSessionKey = USER_SESSION_KEY_PREFIX + userId;
        Object sessionId = redisTemplate.opsForValue().get(userSessionKey);
        if (sessionId instanceof String existingSessionId) {
            redisTemplate.delete(SESSION_KEY_PREFIX + existingSessionId);
        }
        redisTemplate.delete(userSessionKey);
    }

    private void storeSessionInMemory(String sessionId, UUID userId, Duration ttl) {
        Instant expiresAt = Instant.now().plus(ttl);
        UserSessionRecord existingSession = inMemoryUserSessions.get(userId);
        if (existingSession != null && existingSession.isActive()) {
            inMemorySessions.remove(existingSession.sessionId());
        }
        inMemorySessions.put(sessionId, new SessionRecord(userId, expiresAt));
        inMemoryUserSessions.put(userId, new UserSessionRecord(sessionId, expiresAt));
    }

    private Optional<UUID> getUserIdForSessionInMemory(String sessionId) {
        SessionRecord record = inMemorySessions.get(sessionId);
        if (record == null || record.isExpired()) {
            inMemorySessions.remove(sessionId);
            return Optional.empty();
        }
        return Optional.of(record.userId());
    }

    private void invalidateSessionInMemory(String sessionId) {
        SessionRecord record = inMemorySessions.remove(sessionId);
        if (record != null) {
            UserSessionRecord userRecord = inMemoryUserSessions.get(record.userId());
            if (userRecord != null && sessionId.equals(userRecord.sessionId())) {
                inMemoryUserSessions.remove(record.userId());
            }
        }
    }

    private void invalidateUserSessionsInMemory(UUID userId) {
        UserSessionRecord record = inMemoryUserSessions.remove(userId);
        if (record != null) {
            inMemorySessions.remove(record.sessionId());
        }
    }

    private record SessionRecord(UUID userId, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private record UserSessionRecord(String sessionId, Instant expiresAt) {
        boolean isActive() {
            return Instant.now().isBefore(expiresAt);
        }
    }
}
