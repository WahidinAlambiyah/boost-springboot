package com.example.purchaseorder.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class PurchaseOrderDetailRequest {
    private Long itemId;
    private Integer itemQty;
    private BigDecimal itemCost;
    private BigDecimal itemPrice;
}
