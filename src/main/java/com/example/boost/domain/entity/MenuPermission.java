package com.example.boost.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "menu_permissions", schema = "fastworks_springboot", uniqueConstraints = {
        @UniqueConstraint(name = "uk_menu_permissions_menu_permission", columnNames = {"menu_id", "permission_id"})
})
public class MenuPermission {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false, foreignKey = @ForeignKey(name = "fk_menu_permissions_menu"))
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_menu_permissions_permission"))
    private Permission permission;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_mode", nullable = false, length = 30)
    private MenuMatchMode matchMode = MenuMatchMode.ANY;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (matchMode == null) {
            matchMode = MenuMatchMode.ANY;
        }
    }
}
