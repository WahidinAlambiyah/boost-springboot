package com.example.boost.domain.dto;

import com.example.boost.domain.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String phoneNumber;
    private String fullName;
    private boolean active;
    private int failedLoginCount;
    private OffsetDateTime lockedUntil;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime passwordChangedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<Role> roles;
}
