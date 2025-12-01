package com.example.boost.event.service;

import com.example.boost.event.model.DomainEvent;
import com.example.boost.event.repository.DomainEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class DomainEventService {
    private final DomainEventRepository repository;
    private final Supplier<String> ulidSupplier;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DomainEventService(DomainEventRepository repository, Supplier<String> ulidSupplier) {
        this.repository = repository;
        this.ulidSupplier = ulidSupplier;
    }

    public void record(String aggregateType, String aggregateId, String type, Object payload) {
        DomainEvent event = new DomainEvent();
        event.setId(ulidSupplier.get());
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setType(type);
        event.setPayload(toJson(payload));
        repository.save(event);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
