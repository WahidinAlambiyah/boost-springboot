package com.example.boost.domain.mapper;

import com.example.boost.domain.dto.RoleResponse;
import com.example.boost.domain.entity.Role;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleMapper {
    public RoleResponse toResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setCode(role.getCode());
        response.setName(role.getName());
        if (role.getRolePermissions() == null) {
            response.setPermissions(Set.of());
        } else {
            response.setPermissions(role.getRolePermissions().stream()
                    .map(rolePermission -> rolePermission.getPermission().getCode())
                    .collect(Collectors.toSet()));
        }
        return response;
    }
}
