package com.example.workorder.controller;

import com.example.workorder.security.UserRole;

import java.util.Optional;

public class RequestContext {
    private final UserRole role;
    private final Optional<Long> divisionId;

    public RequestContext(UserRole role, Optional<Long> divisionId) {
        this.role = role;
        this.divisionId = divisionId;
    }

    public UserRole getRole() {
        return role;
    }

    public Optional<Long> getDivisionId() {
        return divisionId;
    }
}
