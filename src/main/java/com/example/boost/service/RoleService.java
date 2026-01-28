package com.example.boost.service;

import com.example.boost.domain.dto.RoleCreateRequest;
import com.example.boost.domain.dto.RoleUpdateRequest;
import com.example.boost.domain.entity.Permission;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.RolePermission;
import com.example.boost.domain.entity.RolePermissionId;
import com.example.boost.exception.ConflictException;
import com.example.boost.exception.NotFoundException;
import com.example.boost.repository.PermissionRepository;
import com.example.boost.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    public Role getById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));
    }

    @Transactional
    public Role createRole(RoleCreateRequest request) {
        if (roleRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Role code already exists");
        }
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setActive(request.getIsActive() == null || request.getIsActive());
        return roleRepository.save(role);
    }

    @Transactional
    public Role updateRole(UUID id, RoleUpdateRequest request) {
        Role role = getById(id);
        if (request.getName() != null) {
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            role.setActive(request.getIsActive());
        }
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(UUID id) {
        Role role = getById(id);
        roleRepository.delete(role);
    }

    @Transactional
    public Role assignPermissions(UUID roleId, Set<String> permissionCodes) {
        Role role = getById(roleId);
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
                    .build();
            role.getRolePermissions().add(rolePermission);
        }
        return roleRepository.save(role);
    }
}
