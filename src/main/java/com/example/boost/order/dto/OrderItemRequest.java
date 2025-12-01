package com.example.boost.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderItemRequest {
    @NotBlank
    private String productId;

    @NotBlank
    private String productName;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private Double price;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
