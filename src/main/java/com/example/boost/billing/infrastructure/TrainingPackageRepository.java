package com.example.boost.billing.infrastructure;

import com.example.boost.domain.entity.TrainingPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingPackageRepository extends JpaRepository<TrainingPackage, UUID> {
    List<TrainingPackage> findByAcademyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID academyId);

    List<TrainingPackage> findByAcademyIdAndIsActiveTrueAndDeletedAtIsNullOrderByCreatedAtDesc(UUID academyId);

    Optional<TrainingPackage> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByAcademyIdAndCodeIgnoreCaseAndDeletedAtIsNull(UUID academyId, String code);

    boolean existsByAcademyIdAndCodeIgnoreCaseAndDeletedAtIsNullAndIdNot(UUID academyId, String code, UUID id);
}
