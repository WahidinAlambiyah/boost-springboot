package com.example.boost.order.repository;

import com.example.boost.order.model.Order;
import com.example.boost.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUser(User user);
}
