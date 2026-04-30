package com.example.boost.assessment.infrastructure;

import com.example.boost.domain.entity.StudentAssessment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(value = "select exists(select 1 from fastworks_springboot.students s where s.id = :studentId and s.academy_id = :academyId)", nativeQuery = true)
    boolean existsStudentInAcademy(@Param("studentId") UUID studentId, @Param("academyId") UUID academyId);

    @Query(value = "select exists(select 1 from fastworks_springboot.coach_profiles c where c.id = :coachId and c.academy_id = :academyId and c.deleted_at is null)", nativeQuery = true)
    boolean existsCoachInAcademy(@Param("coachId") UUID coachId, @Param("academyId") UUID academyId);

    @Query(value = "select exists(select 1 from fastworks_springboot.class_sessions cs where cs.id = :classSessionId and cs.academy_id = :academyId and cs.deleted_at is null)", nativeQuery = true)
    boolean existsClassSessionInAcademy(@Param("classSessionId") UUID classSessionId, @Param("academyId") UUID academyId);
}
