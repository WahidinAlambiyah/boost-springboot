package com.example.boost.event.repository;

import com.example.boost.event.model.DomainEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainEventRepository extends JpaRepository<DomainEvent, String> {
}
