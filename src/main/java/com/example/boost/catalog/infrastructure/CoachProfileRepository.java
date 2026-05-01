package com.example.boost.catalog.infrastructure;

import com.example.boost.common.exception.ConflictException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.entity.CoachProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CoachProfileRepository extends JpaRepository<CoachProfile, UUID> {

    Page<CoachProfile> findByAcademyId(UUID academyId, Pageable pageable);

    Page<CoachProfile> findByAcademyIdAndCoachNoContainingIgnoreCaseOrAcademyIdAndFullNameContainingIgnoreCase(
            UUID academyId,
            String coachNoKeyword,
            UUID sameAcademyId,
            String fullNameKeyword,
            Pageable pageable);

    Optional<CoachProfile> findByIdAndDeletedAtIsNull(UUID id);

    Optional<CoachProfile> findByIdAndAcademyIdAndDeletedAtIsNull(UUID id, UUID academyId);

    java.util.List<CoachProfile> findByAcademyIdAndIsActiveTrueAndDeletedAtIsNull(UUID academyId);

    boolean existsByAcademyIdAndUserIdAndDeletedAtIsNull(UUID academyId, UUID userId);

    default CoachProfile getActiveByIdOrThrow(UUID id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Coach profile not found"));
    }

    default CoachProfile getActiveByIdAndAcademyIdOrThrow(UUID id, UUID academyId) {
        return findByIdAndAcademyIdAndDeletedAtIsNull(id, academyId)
                .orElseThrow(() -> new NotFoundException("Coach profile not found"));
    }

    default void ensureActiveUniqueCoachUser(UUID academyId, UUID userId) {
        if (existsByAcademyIdAndUserIdAndDeletedAtIsNull(academyId, userId)) {
            throw new ConflictException("Coach already exists for this academy and user");
        }
    }
}
