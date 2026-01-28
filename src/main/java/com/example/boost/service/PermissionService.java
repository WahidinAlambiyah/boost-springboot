package com.example.boost.service;

import com.example.boost.domain.dto.PermissionCreateRequest;
import com.example.boost.domain.dto.PermissionUpdateRequest;
import com.example.boost.domain.entity.Permission;
import com.example.boost.exception.ConflictException;
import com.example.boost.exception.NotFoundException;
import com.example.boost.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public List<Permission> getPermissions() {
        return permissionRepository.findAll();
    }

    public Permission getById(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission not found"));
    }

    @Transactional
    public Permission createPermission(PermissionCreateRequest request) {
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Permission code already exists");
        }
        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setModule(request.getModule());
        permission.setDescription(request.getDescription());
        permission.setActive(request.getIsActive() == null || request.getIsActive());
        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission updatePermission(UUID id, PermissionUpdateRequest request) {
        Permission permission = getById(id);
        if (request.getName() != null) {
            permission.setName(request.getName());
        }
        if (request.getModule() != null) {
            permission.setModule(request.getModule());
        }
        if (request.getDescription() != null) {
            permission.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            permission.setActive(request.getIsActive());
        }
        return permissionRepository.save(permission);
    }

    @Transactional
    public void deletePermission(UUID id) {
        Permission permission = getById(id);
        permissionRepository.delete(permission);
    }
}
