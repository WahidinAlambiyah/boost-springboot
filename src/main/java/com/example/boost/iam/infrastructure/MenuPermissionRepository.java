package com.example.boost.iam.infrastructure;

import com.example.boost.domain.entity.MenuPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MenuPermissionRepository extends JpaRepository<MenuPermission, UUID> {
    boolean existsByMenuIdAndPermissionId(UUID menuId, UUID permissionId);

    Optional<MenuPermission> findByMenuIdAndPermissionId(UUID menuId, UUID permissionId);

    void deleteByMenuIdAndPermissionId(UUID menuId, UUID permissionId);
}
