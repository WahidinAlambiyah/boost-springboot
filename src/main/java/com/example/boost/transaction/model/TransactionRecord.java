package com.example.boost.transaction.model;

import com.example.boost.order.model.Order;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "transactions")
public class TransactionRecord {
    @Id
    private String id;

    @ManyToOne(optional = false)
    private Order order;

    private Double amount;
    private String status;
    private Instant createdAt;

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
