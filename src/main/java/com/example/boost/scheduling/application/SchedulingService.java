package com.example.boost.scheduling.application;

import com.example.boost.domain.dto.SchedulingSummaryResponse;
import com.example.boost.notification.application.OutboxEventService;
import com.example.boost.scheduling.infrastructure.SchedulingRepository;
import com.example.boost.security.CurrentActor;
import com.example.boost.security.CurrentActorProvider;
import com.example.boost.security.ScopeGuardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final CurrentActorProvider currentActorProvider;
    private final ScopeGuardRepository scopeGuardRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    public List<SchedulingSummaryResponse> getSummaries() {
        CurrentActor actor = currentActorProvider.getCurrentActor();
        return schedulingRepository.findSummaries(actor.userId(), actor.hasRole("INSTRUCTOR"));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCHEDULE_WRITE')")
    public long getWritableSummaryCount() {
        CurrentActor actor = currentActorProvider.getCurrentActor();
        return schedulingRepository.findSummaries(actor.userId(), actor.hasRole("INSTRUCTOR")).size();
    }

    @Transactional
    @PreAuthorize("hasAuthority('SCHEDULE_WRITE')")
    public void reschedule(UUID classGroupId, String previousStartAt, String newStartAt, String reason) {
        CurrentActor actor = currentActorProvider.getCurrentActor();
        if (actor.hasRole("INSTRUCTOR") && !scopeGuardRepository.instructorOwnsClassGroup(actor.userId(), classGroupId)) {
            throw new AccessDeniedException("Forbidden");
        }

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
