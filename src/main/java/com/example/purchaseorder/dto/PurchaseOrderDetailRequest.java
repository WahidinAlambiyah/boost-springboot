package com.example.purchaseorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PurchaseOrderDetailRequest {
    private Long itemId;
    private Integer itemQty;
    private BigDecimal itemCost;
    private BigDecimal itemPrice;
    private String createdBy;
    private OffsetDateTime createdDatetime;
    private String updatedBy;
    private OffsetDateTime updatedDatetime;
}
