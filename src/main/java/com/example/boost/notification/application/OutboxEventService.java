package com.example.boost.notification.application;

import com.example.boost.context.RequestContext;
import com.example.boost.context.RequestContextData;
import com.example.boost.domain.entity.OutboxEvent;
import com.example.boost.notification.infrastructure.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxEventService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void append(String aggregateType, UUID aggregateId, String eventType, Map<String, Object> payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setOccurredAt(OffsetDateTime.now());
        event.setCorrelationId(MDC.get("correlationId"));

        RequestContextData requestContext = RequestContext.get();
        if (requestContext != null) {
            event.setRequestId(requestContext.getRequestId());
        }

        try {
            event.setPayload(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
        outboxEventRepository.save(event);
    }
}
