package com.example.graphqlusers.auth;

import com.example.graphqlusers.app.Role;

import java.util.Set;
import java.util.UUID;

public record AuthContext(
        UUID userId,
        Set<Role> roles,
        String token
) {
    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }
}
