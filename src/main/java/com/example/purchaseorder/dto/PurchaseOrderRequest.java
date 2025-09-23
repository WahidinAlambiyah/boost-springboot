package com.example.purchaseorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PurchaseOrderRequest {
    private OffsetDateTime datetime;
    private String description;
    private BigDecimal totalPrice;
    private BigDecimal totalCost;
    private List<PurchaseOrderDetailRequest> details;
}
