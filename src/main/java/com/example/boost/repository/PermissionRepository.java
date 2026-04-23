package com.example.boost.repository;

import com.example.boost.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    List<Permission> findByCodeIn(Set<String> codes);

    @Query("""
        select distinct p.code
        from Permission p
        join p.rolePermissions rp
        join rp.role r
        join r.userRoles ur
        where ur.user.id = :userId
          and r.isActive = true
          and p.isActive = true
        """)
    List<String> findCodesByUserId(@Param("userId") UUID userId);
}
