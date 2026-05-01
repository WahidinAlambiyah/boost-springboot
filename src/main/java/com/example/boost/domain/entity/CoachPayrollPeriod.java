package com.example.boost.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "coach_payroll_periods", schema = "fastworks_springboot")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class CoachPayrollPeriod {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academy_id", nullable = false)
    private Academy academy;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "generated_at")
    private OffsetDateTime generatedAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

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
}
