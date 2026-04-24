package com.example.boost.scheduling.application;

import com.example.boost.domain.dto.SchedulingSummaryResponse;
import com.example.boost.notification.application.OutboxEventService;
import com.example.boost.scheduling.infrastructure.SchedulingRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchedulingService {
    private final SchedulingRepository schedulingRepository;
    private final OutboxEventService outboxEventService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    public List<SchedulingSummaryResponse> getSummaries() {
        return schedulingRepository.findSummaries();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCHEDULE_WRITE')")
    public long getWritableSummaryCount() {
        return schedulingRepository.findSummaries().size();
    }

    @Transactional
    @PreAuthorize("hasAuthority('SCHEDULE_WRITE')")
    public void reschedule(UUID classGroupId, String previousStartAt, String newStartAt, String reason) {
        outboxEventService.append(
                "CLASS_GROUP",
                classGroupId,
                "class.rescheduled",
                Map.of(
                        "classGroupId", classGroupId,
                        "previousStartAt", previousStartAt,
                        "newStartAt", newStartAt,
                        "reason", reason == null ? "" : reason
                )
        );
    }
}
