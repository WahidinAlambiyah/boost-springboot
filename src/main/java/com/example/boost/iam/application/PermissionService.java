package com.example.boost.iam.application;

import com.example.boost.domain.dto.PermissionCreateRequest;
import com.example.boost.domain.dto.PermissionResponse;
import com.example.boost.domain.dto.PermissionUpdateRequest;
import com.example.boost.domain.dto.IdNameResponse;
import com.example.boost.domain.entity.Permission;
import com.example.boost.domain.mapper.PermissionMapper;
import com.example.boost.common.exception.ConflictException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.iam.infrastructure.PermissionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Transactional(readOnly = true)
    public Page<PermissionResponse> getPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable)
                .map(permissionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<IdNameResponse> getPermissionLookups() {
        return permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(permissionMapper::toLookupResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PermissionResponse getById(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission not found"));
        return permissionMapper.toResponse(permission);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERMISSION_WRITE')")
    public PermissionResponse createPermission(PermissionCreateRequest request) {
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Permission code already exists");
        }
        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setModule(request.getModule());
        permission.setDescription(request.getDescription());
        permission.setActive(request.getIsActive() == null || request.getIsActive());
        Permission saved = permissionRepository.save(permission);
        return permissionMapper.toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERMISSION_WRITE')")
    public PermissionResponse updatePermission(UUID id, PermissionUpdateRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission not found"));
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
        Permission saved = permissionRepository.save(permission);
        return permissionMapper.toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public void deletePermission(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission not found"));
        permissionRepository.delete(permission);
    }
}
