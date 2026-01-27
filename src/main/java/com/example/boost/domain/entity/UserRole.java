package com.example.boost.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_roles", schema = "fastworks_springboot")
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", referencedColumnName="id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_roles_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", referencedColumnName="id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_roles_role"))
    private Role role;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();

    // optional: assigned_by -> users.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", foreignKey = @ForeignKey(name = "fk_user_roles_assigned_by"))
    private User assignedBy;

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) assignedAt = Instant.now();
        if (id == null && user != null && role != null) {
            id = new UserRoleId(user.getId(), role.getId());
        }
    }
}
