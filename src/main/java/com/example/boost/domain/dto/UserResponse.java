package com.example.boost.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    @JsonProperty("isActive")
    private boolean isActive;
    private Set<String> roles;
    private Set<UserRoleAssignmentResponse> roleAssignments;
    private Set<String> permissions;
    private OffsetDateTime createdAt;
}
