package com.example.boost.assessment.infrastructure;

import com.example.boost.domain.entity.StudentAssessmentScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface StudentAssessmentScoreRepository extends JpaRepository<StudentAssessmentScore, UUID> {
    @Query(value = """
            select
                sk.id as skillId,
                sk.code as skillCode,
                sk.name as skillName,
                count(ss.id) as assessmentCount,
                avg(ss.score) as averageScore
            from fastworks_springboot.student_assessment_scores ss
            join fastworks_springboot.student_assessments sa on sa.id = ss.assessment_id
            join fastworks_springboot.assessment_skills sk on sk.id = ss.skill_id
            where ss.deleted_at is null
              and sa.deleted_at is null
              and sk.deleted_at is null
              and (:studentId is null or sa.student_id = :studentId)
              and (:classSessionId is null or sa.class_session_id = :classSessionId)
              and (:from is null or sa.assessed_at >= :from)
              and (:to is null or sa.assessed_at <= :to)
            group by sk.id, sk.code, sk.name
            order by sk.code asc
            """, nativeQuery = true)
    List<SkillProgressAggregate> aggregateSkillProgress(UUID studentId, UUID classSessionId, OffsetDateTime from, OffsetDateTime to);
}
