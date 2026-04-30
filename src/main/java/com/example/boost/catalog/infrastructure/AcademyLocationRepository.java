package com.example.boost.catalog.infrastructure;

import com.example.boost.common.exception.ConflictException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.entity.AcademyLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcademyLocationRepository extends JpaRepository<AcademyLocation, UUID> {

    Page<AcademyLocation> findByAcademyId(UUID academyId, Pageable pageable);

    Page<AcademyLocation> findByAcademyIdAndCodeContainingIgnoreCaseOrAcademyIdAndNameContainingIgnoreCase(
            UUID academyId,
            String codeKeyword,
            UUID sameAcademyId,
            String nameKeyword,
            Pageable pageable);

    Optional<AcademyLocation> findByIdAndDeletedAtIsNull(UUID id);

    Optional<AcademyLocation> findByIdAndAcademyIdAndDeletedAtIsNull(UUID id, UUID academyId);

    boolean existsByAcademyIdAndCodeIgnoreCaseAndDeletedAtIsNull(UUID academyId, String code);

    default AcademyLocation getActiveByIdOrThrow(UUID id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Academy location not found"));
    }

    default AcademyLocation getActiveByIdAndAcademyIdOrThrow(UUID id, UUID academyId) {
        return findByIdAndAcademyIdAndDeletedAtIsNull(id, academyId)
                .orElseThrow(() -> new NotFoundException("Academy location not found"));
    }

    default void ensureActiveCodeUnique(UUID academyId, String code) {
        if (existsByAcademyIdAndCodeIgnoreCaseAndDeletedAtIsNull(academyId, code)) {
            throw new ConflictException("Academy location code already exists");
        }
    }
}
