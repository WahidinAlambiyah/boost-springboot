package com.example.boost.security;

import java.util.Set;
import java.util.UUID;

public record CurrentActor(
        UUID userId,
        String email,
        Set<String> roleCodes
) {
    public boolean hasRole(String roleCode) {
        return roleCodes != null && roleCodes.contains(roleCode);
    }
}
