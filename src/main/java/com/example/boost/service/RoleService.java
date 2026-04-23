package com.example.boost.service;

import com.example.boost.domain.dto.RoleCreateRequest;
import com.example.boost.domain.dto.RoleResponse;
import com.example.boost.domain.dto.RoleUpdateRequest;
import com.example.boost.domain.dto.IdNameResponse;
import com.example.boost.domain.entity.Permission;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.RolePermission;
import com.example.boost.domain.entity.RolePermissionId;
import com.example.boost.domain.mapper.RoleMapper;
import com.example.boost.exception.ConflictException;
import com.example.boost.exception.NotFoundException;
import com.example.boost.repository.PermissionRepository;
import com.example.boost.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditLogService auditLogService;
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    public Page<RoleResponse> getRoles(Pageable pageable) {
        return roleRepository.findAll(pageable)
                .map(roleMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<IdNameResponse> getRoleLookups() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(roleMapper::toLookupResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getById(UUID id) {
        Role role = roleRepository.findWithPermissionsById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        return roleMapper.toResponse(role);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public RoleResponse createRole(RoleCreateRequest request) {
        if (roleRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Role code already exists");
        }
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setActive(request.getIsActive() == null || request.getIsActive());
        Role saved = roleRepository.save(role);
        return roleMapper.toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public RoleResponse updateRole(UUID id, RoleUpdateRequest request) {
        Role role = roleRepository.findWithPermissionsById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        if (request.getName() != null) {
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            role.setActive(request.getIsActive());
        }
        Role saved = roleRepository.save(role);
        return roleMapper.toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public void deleteRole(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        roleRepository.delete(role);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public RoleResponse assignPermissions(UUID roleId, Set<String> permissionCodes) {
        Role role = roleRepository.findWithPermissionsById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        Set<String> beforePermissions = role.getRolePermissions().stream()
                .map(rolePermission -> rolePermission.getPermission().getCode())
                .collect(java.util.stream.Collectors.toSet());
        List<Permission> permissions = permissionRepository.findByCodeIn(permissionCodes);
        if (permissions.size() != permissionCodes.size()) {
            Set<String> found = new HashSet<>();
            for (Permission permission : permissions) {
                found.add(permission.getCode());
            }
            Set<String> missing = new HashSet<>(permissionCodes);
            missing.removeAll(found);
            throw new NotFoundException("Permission codes not found: " + missing);
        }
        role.getRolePermissions().clear();
        for (Permission permission : permissions) {
            RolePermission rolePermission = RolePermission.builder()
                    .role(role)
                    .permission(permission)
                    .id(new RolePermissionId(role.getId(), permission.getId()))
                    .assignedAt(java.time.OffsetDateTime.now())
                    .build();
            role.getRolePermissions().add(rolePermission);
        }
        Role saved = roleRepository.save(role);
        Set<String> afterPermissions = saved.getRolePermissions().stream()
                .map(rolePermission -> rolePermission.getPermission().getCode())
                .collect(java.util.stream.Collectors.toSet());
        Set<String> added = new HashSet<>(afterPermissions);
        added.removeAll(beforePermissions);
        Set<String> removed = new HashSet<>(beforePermissions);
        removed.removeAll(afterPermissions);
        if (!added.isEmpty()) {
            auditLogService.rbacEvent("ROLE_PERMISSION_ADDED")
                    .entity("ROLE", saved.getId().toString())
                    .metadata(java.util.Map.of("permissions", added))
                    .statusSuccess()
                    .save();
        }
        if (!removed.isEmpty()) {
            auditLogService.rbacEvent("ROLE_PERMISSION_REMOVED")
                    .entity("ROLE", saved.getId().toString())
                    .metadata(java.util.Map.of("permissions", removed))
                    .statusSuccess()
                    .save();
        }
        return roleMapper.toResponse(saved);
    }
}
