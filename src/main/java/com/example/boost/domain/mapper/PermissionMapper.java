package com.example.boost.domain.mapper;

import com.example.boost.domain.dto.PermissionResponse;
import com.example.boost.domain.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {
    public PermissionResponse toResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setCode(permission.getCode());
        response.setName(permission.getName());
        response.setModule(permission.getModule());
        response.setActive(permission.isActive());
        return response;
    }
}
