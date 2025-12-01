package com.example.boost.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderUpdateRequest {
    @NotNull
    private String status;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}
