package com.example.boost.dto;

import java.time.Instant;
import java.util.Set;

public class AuthResponse {
    private String token;
    private Instant expiresAt;
    private String sessionId;
    private Set<String> roles;

    public AuthResponse(String token, Instant expiresAt, String sessionId, Set<String> roles) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.sessionId = sessionId;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
