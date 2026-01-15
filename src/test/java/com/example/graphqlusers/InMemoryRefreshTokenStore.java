package com.example.graphqlusers;

import com.example.graphqlusers.redis.RefreshTokenStore;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRefreshTokenStore implements RefreshTokenStore {
    private final Map<String, UUID> tokens = new ConcurrentHashMap<>();

    @Override
    public String issue(UUID userId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, userId);
        return token;
    }

    @Override
    public Optional<UUID> getUserId(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    @Override
    public void revoke(String token) {
        tokens.remove(token);
    }
}
