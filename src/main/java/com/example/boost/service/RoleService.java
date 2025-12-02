package com.example.boost.service;

import com.example.boost.domain.Role;
import com.example.boost.dto.RoleRequest;
import com.example.boost.dto.RoleResponse;
import com.example.boost.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    public Optional<RoleResponse> getRole(UUID id) {
        return roleRepository.findById(id).map(this::mapToResponse);
    }

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RoleResponse createRole(RoleRequest request) {
        Role role = new Role(request.getName().toUpperCase(), request.getDescription());
        Role saved = roleRepository.save(role);
        return mapToResponse(saved);
    }

    @Transactional
    public Optional<RoleResponse> updateRole(UUID id, RoleRequest request) {
        return roleRepository.findById(id).map(role -> {
            role.setName(request.getName().toUpperCase());
            role.setDescription(request.getDescription());
            return mapToResponse(role);
        });
    }

    public void deleteRole(UUID id) {
        roleRepository.deleteById(id);
    }

    @Transactional
    public Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            roleRepository.findByName("USER").ifPresent(roles::add);
            return roles;
        }

        for (String roleName : roleNames) {
            roleRepository.findByName(roleName.toUpperCase()).ifPresent(roles::add);
        }
        if (roles.isEmpty()) {
            roleRepository.findByName("USER").ifPresent(roles::add);
        }
        return roles;
    }

    private RoleResponse mapToResponse(Role role) {
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setName(role.getName());
        response.setDescription(role.getDescription());
        return response;
    }
}
