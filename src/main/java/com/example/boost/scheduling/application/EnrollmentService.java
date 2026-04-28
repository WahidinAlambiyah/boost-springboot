package com.example.boost.scheduling.application;

import com.example.boost.catalog.application.port.CatalogQueryService;
import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.EnrollmentActionResponse;
import com.example.boost.domain.dto.EnrollmentSummaryResponse;
import com.example.boost.domain.entity.ClassGroup;
import com.example.boost.domain.entity.Enrollment;
import com.example.boost.domain.entity.EnrollmentStatus;
import com.example.boost.notification.application.OutboxEventService;
import com.example.boost.scheduling.infrastructure.EnrollmentJpaRepository;
import com.example.boost.scheduling.infrastructure.EnrollmentRepository;
import com.example.boost.security.CurrentActor;
import com.example.boost.security.CurrentActorProvider;
import com.example.boost.security.ScopeGuardRepository;
import com.example.boost.service.DomainMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private static final String ENROLL_SCOPE = "ENROLL_REGISTER";

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentJpaRepository enrollmentJpaRepository;
    private final CatalogQueryService catalogQueryService;
    private final IdempotencyService idempotencyService;
    private final OutboxEventService outboxEventService;
    private final DomainMetricsService domainMetricsService;
    private final CurrentActorProvider currentActorProvider;
    private final ScopeGuardRepository scopeGuardRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ENROLLMENT_READ')")
    public List<EnrollmentSummaryResponse> getSummaries() {
        CurrentActor actor = currentActorProvider.getCurrentActor();
        return enrollmentRepository.findSummaries(actor.userId(), actor.hasRole("GUARDIAN"));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ENROLLMENT_WRITE')")
    public long getWritableSummaryCount() {
        CurrentActor actor = currentActorProvider.getCurrentActor();
        return enrollmentRepository.findSummaries(actor.userId(), actor.hasRole("GUARDIAN")).size();
    }

    @Transactional
    @PreAuthorize("hasAuthority('ENROLLMENT_WRITE')")
    public EnrollmentActionResponse register(UUID studentId, UUID classGroupId, String idempotencyKey) {
        CurrentActor actor = currentActorProvider.getCurrentActor();
        if (actor.hasRole("GUARDIAN") && !scopeGuardRepository.guardianOwnsStudent(actor.userId(), studentId)) {
            throw new AccessDeniedException("Forbidden");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BadRequestException("Idempotency-Key header is required");
        }

        Optional<EnrollmentActionResponse> cached = idempotencyService.getStoredResponse(
                ENROLL_SCOPE + ":" + classGroupId + ":" + studentId,
                idempotencyKey,
                EnrollmentActionResponse.class
        );
        if (cached.isPresent()) {
            return cached.get();
        }

        EnrollmentActionResponse response = registerWithRetry(studentId, classGroupId);
        boolean isActive = EnrollmentStatus.ACTIVE.name().equals(response.status());
        domainMetricsService.recordEnrollment(isActive);
        if (!isActive && response.waitlistPosition() != null) {
            domainMetricsService.recordOverbook(response.waitlistPosition());
        }

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("studentId", studentId);
        eventPayload.put("classGroupId", classGroupId);
        eventPayload.put("status", response.status());
        eventPayload.put("waitlistPosition", response.waitlistPosition());
        outboxEventService.append("ENROLLMENT", response.enrollmentId(), "enrollment.created", eventPayload);
        idempotencyService.saveResponse(ENROLL_SCOPE + ":" + classGroupId + ":" + studentId, idempotencyKey, response);
        return response;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ENROLLMENT_WRITE')")
    public EnrollmentActionResponse cancelAndPromote(UUID enrollmentId) {
        Enrollment enrollment = enrollmentJpaRepository.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));

        boolean wasActive = enrollment.getStatus() == EnrollmentStatus.ACTIVE;
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentJpaRepository.save(enrollment);

        if (!wasActive) {
            return new EnrollmentActionResponse(enrollment.getId(), enrollment.getStatus().name(), null);
        }

        Optional<Enrollment> next = enrollmentJpaRepository
                .findFirstByClassGroupIdAndStatusOrderByWaitlistPositionAsc(enrollment.getClassGroupId(), EnrollmentStatus.WAITLIST);

        if (next.isPresent()) {
            Enrollment promoted = next.get();
            promoted.setStatus(EnrollmentStatus.ACTIVE);
            promoted.setWaitlistPosition(null);
            enrollmentJpaRepository.save(promoted);
            domainMetricsService.updateWaitlistLength(
                    enrollmentJpaRepository.countByClassGroupIdAndStatus(enrollment.getClassGroupId(), EnrollmentStatus.WAITLIST)
            );
            return new EnrollmentActionResponse(promoted.getId(), promoted.getStatus().name(), null);
        }

        domainMetricsService.updateWaitlistLength(
                enrollmentJpaRepository.countByClassGroupIdAndStatus(enrollment.getClassGroupId(), EnrollmentStatus.WAITLIST)
        );
        return new EnrollmentActionResponse(enrollment.getId(), enrollment.getStatus().name(), null);
    }

    private EnrollmentActionResponse registerWithRetry(UUID studentId, UUID classGroupId) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return registerAtomically(studentId, classGroupId);
            } catch (ObjectOptimisticLockingFailureException ex) {
                if (attempt == 3) {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Unreachable enrollment retry state");
    }

    private EnrollmentActionResponse registerAtomically(UUID studentId, UUID classGroupId) {
        Optional<Enrollment> existing = enrollmentJpaRepository.findByStudentIdAndClassGroupId(studentId, classGroupId);
        if (existing.isPresent() && existing.get().getStatus() != EnrollmentStatus.CANCELLED) {
            Enrollment enrollment = existing.get();
            return new EnrollmentActionResponse(enrollment.getId(), enrollment.getStatus().name(), enrollment.getWaitlistPosition());
        }

        ClassGroup classGroup = catalogQueryService.getById(classGroupId);

        if (classGroup.getCapacity() == null || classGroup.getCapacity() <= 0) {
            throw new BadRequestException("Class group capacity is invalid");
        }

        long activeCount = enrollmentJpaRepository.countByClassGroupIdAndStatus(classGroupId, EnrollmentStatus.ACTIVE);
        Enrollment enrollment = existing.orElseGet(Enrollment::new);
        enrollment.setStudentId(studentId);
        enrollment.setClassGroupId(classGroupId);

        if (activeCount < classGroup.getCapacity()) {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setWaitlistPosition(null);
        } else {
            enrollment.setStatus(EnrollmentStatus.WAITLIST);
            int nextPosition = enrollmentJpaRepository.findAllByClassGroupIdAndStatusIn(
                            classGroupId,
                            List.of(EnrollmentStatus.WAITLIST)
                    ).stream()
                    .map(Enrollment::getWaitlistPosition)
                    .filter(java.util.Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(0) + 1;
            enrollment.setWaitlistPosition(nextPosition);
        }

        Enrollment saved = enrollmentJpaRepository.save(enrollment);
        catalogQueryService.touchUpdatedAt(classGroup);

        return new EnrollmentActionResponse(saved.getId(), saved.getStatus().name(), saved.getWaitlistPosition());
    }
}
