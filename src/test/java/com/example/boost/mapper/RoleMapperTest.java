package com.example.boost.mapper;

import com.example.boost.domain.dto.RoleResponse;
import com.example.boost.domain.entity.Permission;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.RolePermission;
import com.example.boost.domain.entity.RolePermissionId;
import com.example.boost.domain.mapper.RoleMapper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoleMapperTest {

    @Test
    void mapsPermissionsWhenPresent() {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode("ADMIN");

        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setCode("USER_READ");

        RolePermission rolePermission = RolePermission.builder()
                .id(new RolePermissionId(role.getId(), permission.getId()))
                .role(role)
                .permission(permission)
                .build();

        role.setRolePermissions(new HashSet<>(Set.of(rolePermission)));

        RoleMapper mapper = new RoleMapper();
        RoleResponse response = mapper.toResponse(role);

        assertThat(response.getPermissions()).contains("USER_READ");
    }
}
