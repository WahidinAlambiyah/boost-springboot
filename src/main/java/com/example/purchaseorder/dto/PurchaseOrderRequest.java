package com.example.purchaseorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PurchaseOrderRequest {

    @NotNull
    private OffsetDateTime datetime;

    private String description;

    @PositiveOrZero
    private BigDecimal totalPrice;

    @PositiveOrZero
    private BigDecimal totalCost;

    @Valid
    @NotEmpty
    private List<PurchaseOrderDetailRequest> details;
}
