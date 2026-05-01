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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "coach_payroll_items", schema = "fastworks_springboot")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class CoachPayrollItem {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private CoachPayrollPeriod payrollPeriod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coach_id", nullable = false)
    private CoachProfile coach;

    @Column(name = "base_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "bonus_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal bonusAmount;

    @Column(name = "deduction_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal deductionAmount;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

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
