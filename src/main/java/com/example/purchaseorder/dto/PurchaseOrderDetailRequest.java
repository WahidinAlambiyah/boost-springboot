package com.example.purchaseorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseOrderDetailRequest {

    @NotNull
    private Long itemId;

    @NotNull
    @Positive
    private Integer itemQty;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Item cost must be greater than zero.")
    private BigDecimal itemCost;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Item price must be greater than zero.")
    private BigDecimal itemPrice;
}
