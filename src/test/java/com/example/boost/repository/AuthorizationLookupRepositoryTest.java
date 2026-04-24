package com.example.boost.repository;

import com.example.boost.iam.infrastructure.PermissionRepository;
import com.example.boost.iam.infrastructure.RoleRepository;
import com.example.boost.iam.infrastructure.UserRepository;

import com.example.boost.domain.entity.Permission;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.RolePermission;
import com.example.boost.domain.entity.RolePermissionId;
import com.example.boost.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb-auth-lookup;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=fastworks_springboot",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema.sql",
        "spring.liquibase.enabled=false",
        "DB_SCHEMA=public",
        "spring.datasource.hikari.schema=public",
        "spring.datasource.hikari.connection-init-sql="
})
class AuthorizationLookupRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        Permission activePermission = permissionRepository.saveAndFlush(createPermission("USER_READ", true));
        Permission inactivePermission = permissionRepository.saveAndFlush(createPermission("USER_DELETE", false));
        Permission permissionOnInactiveRole = permissionRepository.saveAndFlush(createPermission("AUDIT_READ", true));

        Role activeRole = roleRepository.saveAndFlush(createRole("USER", true));
        Role inactiveRole = roleRepository.saveAndFlush(createRole("AUDITOR", false));

        activeRole.getRolePermissions().add(RolePermission.builder()
                .role(activeRole)
                .permission(activePermission)
                .id(new RolePermissionId(activeRole.getId(), activePermission.getId()))
                .assignedAt(OffsetDateTime.now())
                .build());

        activeRole.getRolePermissions().add(RolePermission.builder()
                .role(activeRole)
                .permission(inactivePermission)
                .id(new RolePermissionId(activeRole.getId(), inactivePermission.getId()))
                .assignedAt(OffsetDateTime.now())
                .build());

        inactiveRole.getRolePermissions().add(RolePermission.builder()
                .role(inactiveRole)
                .permission(permissionOnInactiveRole)
                .id(new RolePermissionId(inactiveRole.getId(), permissionOnInactiveRole.getId()))
                .assignedAt(OffsetDateTime.now())
                .build());

        activeRole = roleRepository.saveAndFlush(activeRole);
        inactiveRole = roleRepository.saveAndFlush(inactiveRole);

        User seededUser = new User();
        seededUser.setUsername("demo");
        seededUser.setEmail("demo@example.com");
        seededUser.setPasswordHash("hashed");
        seededUser.setActive(true);
        seededUser.setRoles(Set.of(activeRole, inactiveRole));
        user = userRepository.saveAndFlush(seededUser);
    }

    @Test
    void findRoleCodesByUserIdReturnsOnlyActiveRoles() {
        List<String> roleCodes = roleRepository.findCodesByUserId(user.getId());

        assertThat(roleCodes)
                .containsExactly("USER")
                .doesNotContain("AUDITOR");
    }

    @Test
    void findPermissionCodesByUserIdReturnsOnlyActivePermissionsFromActiveRoles() {
        List<String> permissionCodes = permissionRepository.findCodesByUserId(user.getId());

        assertThat(permissionCodes)
                .containsExactly("USER_READ")
                .doesNotContain("USER_DELETE", "AUDIT_READ");
    }

    private Role createRole(String code, boolean isActive) {
        Role role = new Role();
        role.setCode(code);
        role.setName(code + " Role");
        role.setActive(isActive);
        return role;
    }

    private Permission createPermission(String code, boolean isActive) {
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(code + " Name");
        permission.setModule("AUTH");
        permission.setActive(isActive);
        return permission;
    }
}
