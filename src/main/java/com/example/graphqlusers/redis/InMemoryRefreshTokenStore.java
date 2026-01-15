package com.example.graphqlusers.redis;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRefreshTokenStore implements RefreshTokenStore {
    private final Duration ttl;
    private final Map<String, Entry> tokens = new ConcurrentHashMap<>();

    public InMemoryRefreshTokenStore(Duration ttl) {
        this.ttl = ttl;
    }

    @Override
    public String issue(UUID userId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new Entry(userId, Instant.now().plus(ttl)));
        return token;
    }

    @Override
    public Optional<UUID> getUserId(String token) {
        Entry entry = tokens.get(token);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt().isBefore(Instant.now())) {
            tokens.remove(token);
            return Optional.empty();
        }
        return Optional.of(entry.userId());
    }

    @Override
    public void revoke(String token) {
        tokens.remove(token);
    }

    private record Entry(UUID userId, Instant expiresAt) {
    }
}
