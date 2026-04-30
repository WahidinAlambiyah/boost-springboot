package com.example.boost.assessment.infrastructure;

import com.example.boost.domain.entity.AssessmentSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssessmentSkillRepository extends JpaRepository<AssessmentSkill, UUID> {
    List<AssessmentSkill> findByActiveOrderByOrderNoAscCodeAsc(boolean active);

    List<AssessmentSkill> findByAcademy_IdAndActiveOrderByOrderNoAscCodeAsc(UUID academyId, boolean active);

    List<AssessmentSkill> findByAcademyIsNullAndActiveOrderByOrderNoAscCodeAsc(boolean active);
}
