package com.example.boost.billing.application;

import com.example.boost.billing.infrastructure.TrainingPackageRepository;
import com.example.boost.catalog.infrastructure.AcademyRepository;
import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.ConflictException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.TrainingPackageCreateRequest;
import com.example.boost.domain.dto.TrainingPackageResponse;
import com.example.boost.domain.dto.TrainingPackageUpdateRequest;
import com.example.boost.domain.entity.Academy;
import com.example.boost.domain.entity.TrainingPackage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrainingPackageService {
    private final TrainingPackageRepository trainingPackageRepository;
    private final AcademyRepository academyRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    public List<TrainingPackageResponse> list(UUID academyId, boolean activeOnly) {
        List<TrainingPackage> packages = activeOnly
                ? trainingPackageRepository.findByAcademyIdAndIsActiveTrueAndDeletedAtIsNullOrderByCreatedAtDesc(academyId)
                : trainingPackageRepository.findByAcademyIdAndDeletedAtIsNullOrderByCreatedAtDesc(academyId);
        return packages.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    public TrainingPackageResponse getById(UUID academyId, UUID id) {
        return toResponse(getScopedByIdOrThrow(academyId, id));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    public TrainingPackageResponse create(TrainingPackageCreateRequest request) {
        academyRepository.getActiveByIdOrThrow(request.academyId());
        ensureActiveCodeUnique(request.academyId(), request.code(), null);

        TrainingPackage trainingPackage = new TrainingPackage();
        trainingPackage.setAcademy(resolveAcademy(request.academyId()));
        trainingPackage.setCode(request.code().trim());
        trainingPackage.setName(request.name().trim());
        trainingPackage.setPackageType(request.packageType().trim());
        trainingPackage.setTotalSessions(request.sessionQuota());
        trainingPackage.setPrice(request.price());
        trainingPackage.setDescription(request.description());
        trainingPackage.setIsActive(request.isActive() == null || request.isActive());

        return toResponse(trainingPackageRepository.save(trainingPackage));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    public TrainingPackageResponse update(UUID academyId, UUID id, TrainingPackageUpdateRequest request) {
        TrainingPackage trainingPackage = getScopedByIdOrThrow(academyId, id);
        ensureActiveCodeUnique(academyId, request.code(), id);

        trainingPackage.setCode(request.code().trim());
        trainingPackage.setName(request.name().trim());
        trainingPackage.setPackageType(request.packageType().trim());
        trainingPackage.setTotalSessions(request.sessionQuota());
        trainingPackage.setPrice(request.price());
        trainingPackage.setDescription(request.description());
        if (request.isActive() != null) {
            trainingPackage.setIsActive(request.isActive());
        }

        return toResponse(trainingPackageRepository.save(trainingPackage));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    public void delete(UUID academyId, UUID id) {
        TrainingPackage trainingPackage = getScopedByIdOrThrow(academyId, id);
        trainingPackage.setDeletedAt(OffsetDateTime.now());
        trainingPackageRepository.save(trainingPackage);
    }

    private Academy resolveAcademy(UUID academyId) {
        return academyRepository.getActiveByIdOrThrow(academyId);
    }

    private TrainingPackage getScopedByIdOrThrow(UUID academyId, UUID id) {
        TrainingPackage trainingPackage = trainingPackageRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Training package not found"));
        if (!trainingPackage.getAcademy().getId().equals(academyId)) {
            throw new BadRequestException("Training package does not belong to the academy");
        }
        return trainingPackage;
    }

    private void ensureActiveCodeUnique(UUID academyId, String code, UUID excludeId) {
        String normalizedCode = code == null ? null : code.trim();
        boolean exists = excludeId == null
                ? trainingPackageRepository.existsByAcademyIdAndCodeIgnoreCaseAndDeletedAtIsNull(academyId, normalizedCode)
                : trainingPackageRepository.existsByAcademyIdAndCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(academyId, normalizedCode, excludeId);
        if (exists) {
            throw new ConflictException("Training package code already exists in academy");
        }
    }

    private TrainingPackageResponse toResponse(TrainingPackage trainingPackage) {
        return new TrainingPackageResponse(
                trainingPackage.getId(),
                trainingPackage.getAcademy().getId(),
                trainingPackage.getCode(),
                trainingPackage.getName(),
                trainingPackage.getPackageType(),
                trainingPackage.getPrice(),
                trainingPackage.getTotalSessions(),
                trainingPackage.getDescription(),
                trainingPackage.getIsActive(),
                trainingPackage.getCreatedAt(),
                trainingPackage.getUpdatedAt()
        );
    }
}
