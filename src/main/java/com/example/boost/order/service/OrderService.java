package com.example.boost.order.service;

import com.example.boost.event.service.DomainEventService;
import com.example.boost.order.model.Order;
import com.example.boost.order.model.OrderItem;
import com.example.boost.order.repository.OrderRepository;
import com.example.boost.transaction.model.TransactionRecord;
import com.example.boost.transaction.repository.TransactionRecordRepository;
import com.example.boost.user.model.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final Supplier<String> ulidSupplier;
    private final DomainEventService eventService;
    private final RedisTemplate<String, Object> redisTemplate;

    public OrderService(OrderRepository repository,
                        TransactionRecordRepository transactionRecordRepository,
                        Supplier<String> ulidSupplier,
                        DomainEventService eventService,
                        RedisTemplate<String, Object> redisTemplate) {
        this.repository = repository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.ulidSupplier = ulidSupplier;
        this.eventService = eventService;
        this.redisTemplate = redisTemplate;
    }

    public List<Order> listOrders() {
        return repository.findAll();
    }

    public List<Order> listOrders(User user) {
        return repository.findByUser(user);
    }

    @Transactional
    public Order create(User user, List<OrderItem> items) {
        Order order = new Order();
        order.setId(ulidSupplier.get());
        order.setUser(user);
        order.setStatus("ONGOING");
        order.getItems().addAll(items);
        items.forEach(item -> item.setId(ulidSupplier.get()));
        items.forEach(item -> item.setOrder(order));
        order.setTotal(items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum());
        Order saved = repository.save(order);
        cacheOrder(saved);
        eventService.record("Order", saved.getId(), "ORDER_CREATED", saved);
        recordTransaction(saved, "INITIATED");
        return saved;
    }

    @Transactional
    public Optional<Order> update(String orderId, List<OrderItem> items, String status) {
        return repository.findById(orderId).map(existing -> {
            existing.getItems().clear();
            items.forEach(item -> {
                item.setId(ulidSupplier.get());
                item.setOrder(existing);
            });
            existing.getItems().addAll(items);
            existing.setStatus(status);
            existing.setTotal(items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum());
            Order saved = repository.save(existing);
            cacheOrder(saved);
            eventService.record("Order", saved.getId(), "ORDER_UPDATED", saved);
            recordTransaction(saved, "UPDATED");
            return saved;
        });
    }

    @Transactional
    public boolean delete(String orderId) {
        return repository.findById(orderId).map(order -> {
            repository.delete(order);
            eventService.record("Order", orderId, "ORDER_DELETED", null);
            redisTemplate.delete("order::" + orderId);
            return true;
        }).orElse(false);
    }

    private void cacheOrder(Order saved) {
        redisTemplate.opsForValue().set("order::" + saved.getId(), saved);
    }

    private void recordTransaction(Order order, String status) {
        TransactionRecord record = new TransactionRecord();
        record.setId(ulidSupplier.get());
        record.setOrder(order);
        record.setAmount(order.getTotal());
        record.setStatus(status);
        transactionRecordRepository.save(record);
    }
}
