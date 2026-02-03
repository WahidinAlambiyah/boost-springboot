package com.example.boost.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserRoleAssignmentResponse {
    private UUID roleId;
    private String roleCode;
    private String roleName;
    private OffsetDateTime assignedAt;
    private UUID assignedById;
    private String assignedByUsername;
}
