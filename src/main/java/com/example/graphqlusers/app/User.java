package com.example.graphqlusers.app;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record User(
        UUID id,
        String username,
        String email,
        String fullName,
        String passwordHash,
        Set<Role> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
