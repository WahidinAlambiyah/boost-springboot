package com.example.boost.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", schema = "fastworks_springboot")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class User {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount = 0;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "last_failed_login_at")
    private OffsetDateTime lastFailedLoginAt;

    @Column(name = "disabled_at")
    private OffsetDateTime disabledAt;

    @Column(name = "disabled_reason")
    private String disabledReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Set<Role> getRoles() {
        if (userRoles == null) {
            return Set.of();
        }
        return userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
    }

    public Set<String> getRoleCodes() {
        return getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
    }

    public Set<String> getPermissionCodes() {
        return getRoles().stream()
                .flatMap(role -> {
                    if (role.getRolePermissions() == null) {
                        return java.util.stream.Stream.empty();
                    }
                    return role.getRolePermissions().stream();
                })
                .map(rolePermission -> rolePermission.getPermission().getCode())
                .collect(Collectors.toSet());
    }

    public void setRoles(Set<Role> roles) {
        userRoles.clear();
        if (roles == null) {
            return;
        }
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        for (Role role : roles) {
            UserRole userRole = UserRole.builder()
                    .user(this)
                    .role(role)
                    .id(new UserRoleId(this.id, role.getId()))
                    .build();
            userRoles.add(userRole);
        }
    }
}
