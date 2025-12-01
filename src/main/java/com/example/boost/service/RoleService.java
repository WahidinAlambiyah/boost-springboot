package com.example.boost.service;

import com.example.boost.domain.Role;
import com.example.boost.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
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
}
