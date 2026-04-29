package com.example.boost.iam.infrastructure;

import com.example.boost.domain.entity.Menu;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    @EntityGraph(attributePaths = {"parent", "menuPermissions", "menuPermissions.permission"})
    List<Menu> findByIsActiveTrueAndIsVisibleTrueOrderByOrderNoAsc();
}
