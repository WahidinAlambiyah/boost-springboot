package com.example.graphqlusers.redis;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStore {
    String issue(UUID userId);

    Optional<UUID> getUserId(String token);

    void revoke(String token);
}
