package com.example.boost;

import com.example.boost.domain.dto.AuthResponse;
import com.example.boost.domain.dto.PermissionCreateRequest;
import com.example.boost.domain.dto.PermissionUpdateRequest;
import com.example.boost.domain.dto.RoleCreateRequest;
import com.example.boost.domain.dto.RoleUpdateRequest;
import com.example.boost.domain.dto.UserCreateRequest;
import com.example.boost.domain.dto.UserUpdateRequest;
import com.example.boost.domain.entity.Permission;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.User;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public final class TestDataFactory {
    private TestDataFactory() {
    }

    public static AuthResponse authResponse() {
        return AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .build();
    }

    public static User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("demo");
        user.setEmail("demo@example.com");
        user.setPasswordHash("hashed");
        user.setActive(true);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return user;
    }

    public static UserCreateRequest userCreateRequest() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("demo");
        request.setEmail("demo@example.com");
        request.setPassword("Password123!");
        request.setRoleCodes(Set.of("USER"));
        return request;
    }

    public static UserUpdateRequest userUpdateRequest() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("updated@example.com");
        request.setFullName("Demo User");
        request.setIsActive(true);
        return request;
    }

    public static Role role() {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode("ADMIN");
        role.setName("Admin");
        role.setActive(true);
        return role;
    }

    public static RoleCreateRequest roleCreateRequest() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode("ADMIN");
        request.setName("Admin");
        request.setDescription("Admin role");
        request.setIsActive(true);
        return request;
    }

    public static RoleUpdateRequest roleUpdateRequest() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setName("Updated");
        request.setDescription("Updated role");
        request.setIsActive(true);
        return request;
    }

    public static Permission permission() {
        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setCode("USER_READ");
        permission.setName("User Read");
        permission.setModule("USER");
        permission.setActive(true);
        return permission;
    }

    public static PermissionCreateRequest permissionCreateRequest() {
        PermissionCreateRequest request = new PermissionCreateRequest();
        request.setCode("USER_READ");
        request.setName("User Read");
        request.setModule("USER");
        request.setDescription("Read users");
        request.setIsActive(true);
        return request;
    }

    public static PermissionUpdateRequest permissionUpdateRequest() {
        PermissionUpdateRequest request = new PermissionUpdateRequest();
        request.setName("User Read Updated");
        request.setModule("USER");
        request.setDescription("Updated");
        request.setIsActive(true);
        return request;
    }
}
