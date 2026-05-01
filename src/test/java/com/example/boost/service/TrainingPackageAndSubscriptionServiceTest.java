package com.example.boost.service;

import com.example.boost.billing.application.StudentPackageSubscriptionService;
import com.example.boost.billing.application.TrainingPackageService;
import com.example.boost.billing.infrastructure.StudentAcademyLookupRepository;
import com.example.boost.billing.infrastructure.StudentPackageSubscriptionRepository;
import com.example.boost.billing.infrastructure.TrainingPackageRepository;
import com.example.boost.catalog.infrastructure.AcademyRepository;
import com.example.boost.domain.dto.*;
import com.example.boost.domain.entity.Academy;
import com.example.boost.domain.entity.StudentPackageSubscription;
import com.example.boost.domain.entity.TrainingPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingPackageAndSubscriptionServiceTest {
    @Mock private TrainingPackageRepository trainingPackageRepository;
    @Mock private AcademyRepository academyRepository;
    @Mock private StudentPackageSubscriptionRepository subscriptionRepository;
    @Mock private StudentAcademyLookupRepository studentAcademyLookupRepository;

    @InjectMocks private TrainingPackageService trainingPackageService;
    private StudentPackageSubscriptionService subscriptionService;

    @BeforeEach
    void init() { subscriptionService = new StudentPackageSubscriptionService(subscriptionRepository, trainingPackageRepository, studentAcademyLookupRepository); }

    @Test
    void trainingPackageCreateListUpdateHappyPath() {
        UUID academyId = UUID.randomUUID();
        UUID packageId = UUID.randomUUID();
        Academy academy = new Academy(); academy.setId(academyId);

        TrainingPackage entity = new TrainingPackage();
        entity.setId(packageId); entity.setAcademy(academy); entity.setCode("PKG-1"); entity.setName("Bundle"); entity.setPackageType("SESSION_BUNDLE");
        entity.setTotalSessions(12); entity.setPrice(new BigDecimal("1500000")); entity.setIsActive(true);

        when(academyRepository.getActiveByIdOrThrow(academyId)).thenReturn(academy);
        when(trainingPackageRepository.existsByAcademyIdAndCodeIgnoreCaseAndDeletedAtIsNull(eq(academyId), anyString())).thenReturn(false);
        when(trainingPackageRepository.existsByAcademyIdAndCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(eq(academyId), anyString(), eq(packageId))).thenReturn(false);
        when(trainingPackageRepository.save(any(TrainingPackage.class))).thenReturn(entity);
        when(trainingPackageRepository.findByAcademyIdAndDeletedAtIsNullOrderByCreatedAtDesc(academyId)).thenReturn(List.of(entity));
        when(trainingPackageRepository.findByIdAndDeletedAtIsNull(packageId)).thenReturn(Optional.of(entity));

        TrainingPackageResponse created = trainingPackageService.create(new TrainingPackageCreateRequest(academyId, "PKG-1", "Bundle", "SESSION_BUNDLE", new BigDecimal("1500000"), 12, "Desc", true));
        List<TrainingPackageResponse> listed = trainingPackageService.list(academyId, false);
        TrainingPackageResponse updated = trainingPackageService.update(academyId, packageId, new TrainingPackageUpdateRequest("PKG-1", "Bundle Updated", "SESSION_BUNDLE", new BigDecimal("1500000"), 10, "New", true));

        assertThat(created.id()).isEqualTo(packageId);
        assertThat(listed).hasSize(1);
        assertThat(updated.id()).isEqualTo(packageId);
    }

    @Test
    void studentSubscriptionCreateListUpdateHappyPath() {
        UUID academyId = UUID.randomUUID(); UUID studentId = UUID.randomUUID(); UUID packageId = UUID.randomUUID(); UUID subId = UUID.randomUUID();
        Academy academy = new Academy(); academy.setId(academyId);
        TrainingPackage trainingPackage = new TrainingPackage(); trainingPackage.setId(packageId); trainingPackage.setAcademy(academy); trainingPackage.setPackageType("SESSION_BUNDLE"); trainingPackage.setTotalSessions(8);

        StudentPackageSubscription subscription = new StudentPackageSubscription();
        subscription.setId(subId); subscription.setAcademy(academy); subscription.setStudentId(studentId); subscription.setTrainingPackage(trainingPackage); subscription.setStatus("ACTIVE"); subscription.setRemainingSessions(8);

        when(trainingPackageRepository.findByIdAndDeletedAtIsNull(packageId)).thenReturn(Optional.of(trainingPackage));
        when(studentAcademyLookupRepository.existsByStudentIdAndAcademyId(studentId, academyId)).thenReturn(true);
        when(subscriptionRepository.save(any(StudentPackageSubscription.class))).thenReturn(subscription);
        when(subscriptionRepository.findByStudentIdAndDeletedAtIsNullOrderByCreatedAtDesc(studentId)).thenReturn(List.of(subscription));
        when(subscriptionRepository.findByIdAndDeletedAtIsNull(subId)).thenReturn(Optional.of(subscription));

        StudentPackageSubscriptionResponse created = subscriptionService.create(studentId, new StudentPackageSubscriptionCreateRequest(packageId, LocalDate.now(), LocalDate.now().plusMonths(1)));
        List<StudentPackageSubscriptionResponse> listed = subscriptionService.listByStudent(studentId);
        StudentPackageSubscriptionResponse updated = subscriptionService.update(studentId, subId, new StudentPackageSubscriptionUpdateRequest("INACTIVE", LocalDate.now().plusMonths(2), 6));

        assertThat(created.id()).isEqualTo(subId);
        assertThat(created.remainingSessions()).isEqualTo(8);
        assertThat(listed).hasSize(1);
        assertThat(updated.id()).isEqualTo(subId);
    }
}
