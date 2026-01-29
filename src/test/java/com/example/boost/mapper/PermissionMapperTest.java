package com.example.boost.mapper;

import com.example.boost.domain.dto.PermissionResponse;
import com.example.boost.domain.entity.Permission;
import com.example.boost.domain.mapper.PermissionMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionMapperTest {

    @Test
    void mapsPermissionFields() {
        Permission permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setCode("USER_READ");
        permission.setName("User Read");
        permission.setModule("USER");
        permission.setActive(true);

        PermissionMapper mapper = new PermissionMapper();
        PermissionResponse response = mapper.toResponse(permission);

        assertThat(response.getCode()).isEqualTo("USER_READ");
        assertThat(response.isActive()).isTrue();
    }
}
