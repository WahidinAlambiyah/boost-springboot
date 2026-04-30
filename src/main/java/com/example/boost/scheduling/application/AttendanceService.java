package com.example.boost.scheduling.application;

import com.example.boost.common.exception.NotFoundException;
import com.example.boost.common.exception.UnprocessableEntityException;
import com.example.boost.domain.dto.AttendanceSummaryResponse;
import com.example.boost.domain.dto.AttendanceUpsertRequest;
import com.example.boost.domain.dto.SessionAttendanceStudentResponse;
import com.example.boost.notification.application.OutboxEventService;
import com.example.boost.domain.entity.ClassSession;
import com.example.boost.scheduling.infrastructure.AttendanceRepository;
import com.example.boost.scheduling.infrastructure.ClassSessionRepository;
import com.example.boost.scheduling.infrastructure.EnrollmentJpaRepository;
import com.example.boost.security.CurrentActor;
import com.example.boost.security.CurrentActorProvider;
import com.example.boost.security.ScopeGuardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.example.boost.domain.dto.AttendanceBatchUpsertRequest;
import com.example.boost.domain.dto.AttendanceBatchUpsertResponse;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final OutboxEventService outboxEventService;
    private final CurrentActorProvider currentActorProvider;
    private final ScopeGuardRepository scopeGuardRepository;
    private final ClassSessionRepository classSessionRepository;
    private final EnrollmentJpaRepository enrollmentJpaRepository;

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

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    public List<SessionAttendanceStudentResponse> getSessionAttendances(UUID classSessionId) {
        return attendanceRepository.findByClassSessionId(classSessionId);
    }


    @Transactional
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public AttendanceBatchUpsertResponse upsertSessionAttendanceBatch(UUID classSessionId, AttendanceBatchUpsertRequest request) {
        for (var record : request.records()) {
            upsertStudentAttendance(classSessionId, record.studentId(), record.attendance());
        }
        return new AttendanceBatchUpsertResponse(request.records().size(), "ok");
    }

    @Transactional
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public void upsertStudentAttendance(UUID classSessionId, UUID studentId, AttendanceUpsertRequest request) {
        ClassSession session = classSessionRepository.findByIdAndDeletedAtIsNull(classSessionId)
                .orElseThrow(() -> new NotFoundException("Class session not found"));
        UUID academyId = session.getClassGroup().getAcademy().getId();

        if (!attendanceRepository.existsActiveStudentInAcademy(studentId, academyId)) {
            throw new NotFoundException("Student not found in session academy");
        }

        // NOTE: Trial model is not available yet, so attendance validation currently enforces
        // academy-scope student existence and ACTIVE enrollment in the class group only.
        if (!enrollmentJpaRepository.findByStudentIdAndClassGroupId(studentId, session.getClassGroup().getId())
                .filter(e -> "ACTIVE".equals(e.getStatus().name()))
                .isPresent()) {
            throw new UnprocessableEntityException("Student is not actively enrolled in class group session");
        }

        attendanceRepository.upsertAttendance(classSessionId, studentId, request);
    }
}
