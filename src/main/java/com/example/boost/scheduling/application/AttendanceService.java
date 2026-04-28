package com.example.boost.scheduling.application;

import com.example.boost.domain.dto.AttendanceSummaryResponse;
import com.example.boost.notification.application.OutboxEventService;
import com.example.boost.scheduling.infrastructure.AttendanceRepository;
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
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final OutboxEventService outboxEventService;
    private final CurrentActorProvider currentActorProvider;
    private final ScopeGuardRepository scopeGuardRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    public List<AttendanceSummaryResponse> getSummaries() {
        CurrentActor actor = currentActorProvider.getCurrentActor();
        return attendanceRepository.findSummaries(actor.userId(), actor.hasRole("GUARDIAN"), actor.hasRole("INSTRUCTOR"));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public long getWritableSummaryCount() {
        CurrentActor actor = currentActorProvider.getCurrentActor();
        return attendanceRepository.findSummaries(actor.userId(), actor.hasRole("GUARDIAN"), actor.hasRole("INSTRUCTOR")).size();
    }

    @Transactional
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public void submit(UUID classGroupId, String sessionDate, int presentCount, int absentCount) {
        CurrentActor actor = currentActorProvider.getCurrentActor();
        if (actor.hasRole("INSTRUCTOR") && !scopeGuardRepository.instructorOwnsClassGroup(actor.userId(), classGroupId)) {
            throw new AccessDeniedException("Forbidden");
        }

        outboxEventService.append(
                "ATTENDANCE",
                classGroupId,
                "attendance.submitted",
                Map.of(
                        "classGroupId", classGroupId,
                        "sessionDate", sessionDate,
                        "presentCount", presentCount,
                        "absentCount", absentCount
                )
        );
    }
}
