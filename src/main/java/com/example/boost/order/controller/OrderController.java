package com.example.boost.order.controller;

import com.example.boost.order.dto.OrderItemRequest;
import com.example.boost.order.dto.OrderRequest;
import com.example.boost.order.dto.OrderUpdateRequest;
import com.example.boost.order.model.Order;
import com.example.boost.order.model.OrderItem;
import com.example.boost.order.service.OrderService;
import com.example.boost.user.model.User;
import com.example.boost.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Order> listOrders(@RequestParam(name = "userId", required = false) String userId) {
        if (userId == null) {
            return orderService.listOrders();
        }
        Optional<User> user = userRepository.findById(userId);
        return user.map(orderService::listOrders).orElse(List.of());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OrderRequest request) {
        return userRepository.findById(request.getUserId())
                .map(user -> orderService.create(user, toItems(request.getItems())))
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().body("User not found"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody OrderUpdateRequest request) {
        Optional<Order> updated = orderService.update(id, toItems(request.getItems()), request.getStatus());
        return updated.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean removed = orderService.delete(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private List<OrderItem> toItems(List<OrderItemRequest> items) {
        return items.stream().map(item -> {
            OrderItem entity = new OrderItem();
            entity.setProductId(item.getProductId());
            entity.setProductName(item.getProductName());
            entity.setQuantity(item.getQuantity());
            entity.setPrice(item.getPrice());
            return entity;
        }).collect(Collectors.toList());
    }
}
