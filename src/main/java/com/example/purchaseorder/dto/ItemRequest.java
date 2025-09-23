package com.example.purchaseorder.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class ItemRequest {
    private String name;
    private String description;
    private BigDecimal price;
}
