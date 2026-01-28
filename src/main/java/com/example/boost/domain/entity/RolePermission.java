package com.example.boost.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "role_permissions", schema = "fastworks_springboot")
public class RolePermission {

    @EmbeddedId
    private RolePermissionId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_permissions_role"))
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_permissions_permission"))
    private Permission permission;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = OffsetDateTime.now();
        }
        if (id == null && role != null && permission != null) {
            id = new RolePermissionId(role.getId(), permission.getId());
        }
    }
}
