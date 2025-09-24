package com.example.purchaseorder.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "po_h")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private OffsetDateTime datetime;

    private String description;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_datetime")
    private OffsetDateTime createdDatetime;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_datetime")
    private OffsetDateTime updatedDatetime;

    @Builder.Default
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderDetail> details = new ArrayList<>();

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
