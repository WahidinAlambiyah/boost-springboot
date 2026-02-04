package com.example.boost.repository;

import java.util.List;

import com.example.boost.domain.entity.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role",
            "userRoles.assignedBy",
            "userRoles.role.rolePermissions",
            "userRoles.role.rolePermissions.permission"
    })
    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Override
    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role",
            "userRoles.assignedBy",
            "userRoles.role.rolePermissions",
            "userRoles.role.rolePermissions.permission"
    })
    Page<User> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {
            "userRoles",
            "userRoles.role",
            "userRoles.assignedBy",
            "userRoles.role.rolePermissions",
            "userRoles.role.rolePermissions.permission"
    })
    Optional<User> findWithRolesById(UUID id);

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                        
