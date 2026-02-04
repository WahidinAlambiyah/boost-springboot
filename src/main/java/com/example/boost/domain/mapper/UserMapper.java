package com.example.boost.domain.mapper;

import com.example.boost.domain.dto.UserIdentifierResponse;
import com.example.boost.domain.dto.UserResponse;
import com.example.boost.domain.dto.UserRoleAssignmentResponse;
import com.example.boost.domain.entity.UserRole;
import com.example.boost.domain.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setActive(user.isActive());
        response.setRoles(user.getRoleCodes());
        response.setPermissions(user.getPermissionCodes());
        response.setRoleAssignments(mapRoleAssignments(user));
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    public UserIdentifierResponse toIdentifier(User user) {
        String identifier = user.getEmail();
        if (identifier == null || identifier.isBlank()) {
            identifier = user.getUsername();
        }
        return new UserIdentifierResponse(identifier);
    }

    private Set<UserRoleAssignmentResponse> mapRoleAssignments(User user) {
        if (user.getUserRoles() == null) {
            return Set.of();
        }
        return user.getUserRoles().stream()
                .map(this::toRoleAssignment)
                .collect(Collectors.toSet());
    }

    private UserRoleAssignmentResponse toRoleAssignment(UserRole userRole) {
        UserRoleAssignmentResponse response = new UserRoleAssignmentResponse();
        if (userRole.getRole() != null) {
            response.setRoleId(userRole.getRole().getId());
            response.setRoleCode(userRole.getRole().getCode());
            response.setRoleName(userRole.getRole().getName());
        }
        response.setAssignedAt(userRole.getAssignedAt());
        if (userRole.getAssignedBy() != null) {
            response.setAssignedById(userRole.getAssignedBy().getId());
            response.setAssignedByUsername(userRole.getAssignedBy().getUsername());
        }
        return response;
    }
}
