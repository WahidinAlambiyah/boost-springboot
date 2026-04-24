package com.example.boost.repository;

import com.example.boost.domain.entity.Enrollment;
import com.example.boost.domain.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentJpaRepository extends JpaRepository<Enrollment, UUID> {
    Optional<Enrollment> findByStudentIdAndClassGroupId(UUID studentId, UUID classGroupId);

    long countByClassGroupIdAndStatus(UUID classGroupId, EnrollmentStatus status);

    Optional<Enrollment> findFirstByClassGroupIdAndStatusOrderByWaitlistPositionAsc(UUID classGroupId, EnrollmentStatus status);

    List<Enrollment> findAllByClassGroupIdAndStatusIn(UUID classGroupId, Collection<EnrollmentStatus> statuses);
}
