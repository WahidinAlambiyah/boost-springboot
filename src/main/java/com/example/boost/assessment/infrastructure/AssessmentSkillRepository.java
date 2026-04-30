package com.example.boost.assessment.infrastructure;

import com.example.boost.domain.entity.AssessmentSkill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AssessmentSkillRepository extends JpaRepository<AssessmentSkill, UUID> {
    @Query("""
            select s from AssessmentSkill s
            where (:academyId is null or s.academy.id = :academyId or s.academy is null)
            order by s.orderNo asc nulls last, s.code asc
            """)
    Page<AssessmentSkill> findForList(UUID academyId, Pageable pageable);
    List<AssessmentSkill> findByActiveOrderByOrderNoAscCodeAsc(boolean active);

    List<AssessmentSkill> findByAcademy_IdAndActiveOrderByOrderNoAscCodeAsc(UUID academyId, boolean active);

    List<AssessmentSkill> findByAcademyIsNullAndActiveOrderByOrderNoAscCodeAsc(boolean active);

    List<AssessmentSkill> findByCodeInAndActiveTrue(Collection<String> codes);
}
