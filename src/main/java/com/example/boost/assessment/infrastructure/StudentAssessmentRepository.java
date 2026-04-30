package com.example.boost.assessment.infrastructure;

import com.example.boost.domain.entity.StudentAssessment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentAssessmentRepository extends JpaRepository<StudentAssessment, UUID> {
    @Query("""
            select sa
            from StudentAssessment sa
            where (:studentId is null or sa.studentId = :studentId)
              and (:classSessionId is null or sa.classSessionId = :classSessionId)
              and (:from is null or sa.assessedAt >= :from)
              and (:to is null or sa.assessedAt <= :to)
            order by sa.assessedAt desc
            """)
    List<StudentAssessment> findAssessments(UUID studentId, UUID classSessionId, OffsetDateTime from, OffsetDateTime to);

    @EntityGraph(attributePaths = {"academy", "scores", "scores.skill"})
    Optional<StudentAssessment> findWithScoresById(UUID id);
}
