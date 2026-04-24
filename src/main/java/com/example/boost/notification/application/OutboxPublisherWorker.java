package com.example.boost.notification.application;

import com.example.boost.domain.entity.OutboxEvent;
import com.example.boost.domain.entity.OutboxEventStatus;
import com.example.boost.notification.infrastructure.OutboxEventRepository;
import com.example.boost.service.DomainMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherWorker {
    private final OutboxEventRepository outboxEventRepository;
    private final DomainMetricsService domainMetricsService;

    @Scheduled(fixedDelayString = "${app.outbox.publisher-delay-ms:5000}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxEventRepository.findForPublishing(OutboxEventStatus.NEW, PageRequest.of(0, 50));
        for (OutboxEvent event : pending) {
            try {
                event.setAttempts(event.getAttempts() + 1);
                event.setStatus(OutboxEventStatus.PUBLISHED);
                event.setPublishedAt(OffsetDateTime.now());
                outboxEventRepository.save(event);
                domainMetricsService.recordNotificationDelivery(true);
            } catch (Exception ex) {
                event.setStatus(OutboxEventStatus.FAILED);
                event.setLastError(ex.getMessage());
                outboxEventRepository.save(event);
                domainMetricsService.recordNotificationDelivery(false);
                log.error("Failed to publish outbox event {}", event.getId(), ex);
            }
        }
    }
}
