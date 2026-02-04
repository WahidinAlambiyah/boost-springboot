package com.example.boost.repository;

import com.example.boost.domain.entity.Role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByCode(String code);

    Optional<Role> findByCodeIgnoreCase(String code);

    boolean existsByCode(String code);

    @Query("""
        select distinct r.code
        from Role r
        join r.userRoles ur
        where ur.user.id = :userId
        """)
    List<String> findCodesByUserId(@Param("userId") UUID userId);

    List<Role> findByCodeIn(Set<String> codes);

    @Override
    @EntityGraph(attributePaths = {"rolePermissions", "rolePermissions.permission"})
    List<Role> findAll();

    @Override
    @EntityGraph(attributePaths = {"rolePermissions", "rolePermissions.permission"})
    Page<Role> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"rolePermissions", "rolePermissions.permission"})
    Optional<Role> findWithPermissionsById(UUID id);
}
