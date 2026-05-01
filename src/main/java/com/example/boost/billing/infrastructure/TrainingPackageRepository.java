package com.example.boost.billing.infrastructure;

import com.example.boost.domain.entity.TrainingPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrainingPackageRepository extends JpaRepository<TrainingPackage, UUID> {
    List<TrainingPackage> findByAcademyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID academyId);

    List<TrainingPackage> findByAcademyIdAndIsActiveTrueAndDeletedAtIsNullOrderByCreatedAtDesc(UUID academyId);
}
