package com.example.boost.billing.application;

import com.example.boost.billing.infrastructure.StudentAcademyLookupRepository;
import com.example.boost.billing.infrastructure.StudentPackageSubscriptionRepository;
import com.example.boost.billing.infrastructure.TrainingPackageRepository;
import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.StudentPackageSubscriptionCreateRequest;
import com.example.boost.domain.dto.StudentPackageSubscriptionResponse;
import com.example.boost.domain.dto.StudentPackageSubscriptionUpdateRequest;
import com.example.boost.domain.entity.StudentPackageSubscription;
import com.example.boost.domain.entity.TrainingPackage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentPackageSubscriptionService {
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final StudentPackageSubscriptionRepository subscriptionRepository;
    private final TrainingPackageRepository trainingPackageRepository;
    private final StudentAcademyLookupRepository studentAcademyLookupRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    public List<StudentPackageSubscriptionResponse> listByStudent(UUID studentId) {
        return subscriptionRepository.findByStudentIdAndDeletedAtIsNullOrderByCreatedAtDesc(studentId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    public StudentPackageSubscriptionResponse create(UUID studentId, StudentPackageSubscriptionCreateRequest request) {
        TrainingPackage trainingPackage = trainingPackageRepository.findByIdAndDeletedAtIsNull(request.packageId())
                .orElseThrow(() -> new NotFoundException("Training package not found"));
        validateStudentInAcademy(studentId, trainingPackage.getAcademy().getId());

        StudentPackageSubscription subscription = new StudentPackageSubscription();
        subscription.setAcademy(trainingPackage.getAcademy());
        subscription.setStudentId(studentId);
        subscription.setTrainingPackage(trainingPackage);
        subscription.setStatus(STATUS_ACTIVE);
        subscription.setStartDate(request.startDate());
        subscription.setEndDate(request.endDate());
        subscription.setRemainingSessions(initialRemainingSessions(trainingPackage));

        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    public StudentPackageSubscriptionResponse update(UUID studentId, UUID subscriptionId, StudentPackageSubscriptionUpdateRequest request) {
        StudentPackageSubscription subscription = subscriptionRepository.findByIdAndDeletedAtIsNull(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Student package subscription not found"));
        if (!subscription.getStudentId().equals(studentId)) {
            throw new BadRequestException("Subscription does not belong to student");
        }
        validateStudentInAcademy(studentId, subscription.getAcademy().getId());

        if (request.status() != null && !request.status().isBlank()) {
            subscription.setStatus(request.status().trim().toUpperCase(Locale.ROOT));
        }
        if (request.endDate() != null) {
            subscription.setEndDate(request.endDate());
        }
        if (request.remainingSessions() != null) {
            if (!"SESSION_BUNDLE".equals(subscription.getTrainingPackage().getPackageType())) {
                throw new BadRequestException("remaining_sessions only allowed for SESSION_BUNDLE package type");
            }
            subscription.setRemainingSessions(request.remainingSessions());
        }

        return toResponse(subscriptionRepository.save(subscription));
    }

    private void validateStudentInAcademy(UUID studentId, UUID academyId) {
        if (!studentAcademyLookupRepository.existsByStudentIdAndAcademyId(studentId, academyId)) {
            throw new BadRequestException("Student not found in academy");
        }
    }

    private Integer initialRemainingSessions(TrainingPackage trainingPackage) {
        return "SESSION_BUNDLE".equals(trainingPackage.getPackageType())
                ? trainingPackage.getTotalSessions()
                : null;
    }

    private StudentPackageSubscriptionResponse toResponse(StudentPackageSubscription subscription) {
        return new StudentPackageSubscriptionResponse(
                subscription.getId(),
                subscription.getAcademy().getId(),
                subscription.getStudentId(),
                subscription.getTrainingPackage().getId(),
                subscription.getTrainingPackage().getPackageType(),
                subscription.getStatus(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getRemainingSessions(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
