package com.example.workorder.service;

import com.example.workorder.domain.Access;
import com.example.workorder.domain.Role;
import com.example.workorder.dto.AccessResponse;
import com.example.workorder.dto.RoleRequest;
import com.example.workorder.dto.RoleResponse;
import com.example.workorder.exception.NotFoundException;
import com.example.workorder.repository.AccessRepository;
import com.example.workorder.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final AccessRepository accessRepository;

    public RoleService(RoleRepository roleRepository, AccessRepository accessRepository) {
        this.roleRepository = roleRepository;
        this.accessRepository = accessRepository;
    }

    public RoleResponse createRole(RoleRequest request) {
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        Set<Access> accesses = new HashSet<>();
        if (request.getAccessIds() != null && !request.getAccessIds().isEmpty()) {
            accesses = new HashSet<>(accessRepository.findAllById(request.getAccessIds()));
            if (accesses.size() != request.getAccessIds().size()) {
                throw new NotFoundException("One or more access ids do not exist");
            }
        }
        role.setAccesses(accesses);

        Role saved = roleRepository.save(role);
        return toResponse(saved);
    }

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Role getRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found with id " + id));
    }

    public RoleResponse getRoleResponse(Long id) {
        return toResponse(getRole(id));
    }

    private RoleResponse toResponse(Role role) {
        List<AccessResponse> accessResponses = role.getAccesses().stream()
                .map(access -> new AccessResponse(access.getId(), access.getCode(), access.getDescription()))
                .collect(Collectors.toList());
        return new RoleResponse(role.getId(), role.getName(), role.getDescription(), accessResponses);
    }
}
