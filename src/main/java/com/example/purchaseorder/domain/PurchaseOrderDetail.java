package com.example.purchaseorder.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "po_d")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "poh_id")
    private PurchaseOrderHeader purchaseOrder;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "item_qty", nullable = false)
    private Integer itemQty;

    @Column(name = "item_cost", nullable = false)
    private BigDecimal itemCost;

    @Column(name = "item_price", nullable = false)
    private BigDecimal itemPrice;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_datetime")
    private OffsetDateTime createdDatetime;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_datetime")
    private OffsetDateTime updatedDatetime;
}
