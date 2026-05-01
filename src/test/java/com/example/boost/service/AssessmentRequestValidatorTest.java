package com.example.boost.service;

import com.example.boost.assessment.application.AssessmentRequestValidator;
import com.example.boost.assessment.infrastructure.AssessmentSkillRepository;
import com.example.boost.common.exception.BadRequestException;
import com.example.boost.domain.dto.AssessmentScoreRequest;
import com.example.boost.domain.entity.Academy;
import com.example.boost.domain.entity.AssessmentSkill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentRequestValidatorTest {

    @Mock
    private AssessmentSkillRepository assessmentSkillRepository;

    private AssessmentRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AssessmentRequestValidator(assessmentSkillRepository);
    }

    @Test
    void validateShouldRejectDuplicateSkillCodeInRequest() {
        UUID academyId = UUID.randomUUID();
        List<AssessmentScoreRequest> scores = List.of(
                new AssessmentScoreRequest("FOCUS", BigDecimal.ONE, null),
                new AssessmentScoreRequest("focus", BigDecimal.valueOf(2), null)
        );

        assertThrows(BadRequestException.class, () -> validator.validateAndResolveScores(scores, academyId));
    }

    @Test
    void validateShouldRejectScoreAboveMaxScore() {
        UUID academyId = UUID.randomUUID();
        AssessmentSkill skill = skill("SPEED", 5, academyId);
        when(assessmentSkillRepository.findByCodeInAndActiveTrue(anySet())).thenReturn(List.of(skill));

        List<AssessmentScoreRequest> scores = List.of(new AssessmentScoreRequest("SPEED", BigDecimal.valueOf(5.1), null));

        assertThrows(BadRequestException.class, () -> validator.validateAndResolveScores(scores, academyId));
    }

    @Test
    void validateShouldRejectScoreLessThanOne() {
        UUID academyId = UUID.randomUUID();
        AssessmentSkill skill = skill("SPEED", 5, academyId);
        when(assessmentSkillRepository.findByCodeInAndActiveTrue(anySet())).thenReturn(List.of(skill));

        List<AssessmentScoreRequest> scores = List.of(new AssessmentScoreRequest("SPEED", BigDecimal.ZERO, null));

        assertThrows(BadRequestException.class, () -> validator.validateAndResolveScores(scores, academyId));
    }

    private AssessmentSkill skill(String code, int maxScore, UUID academyId) {
        Academy academy = new Academy();
        academy.setId(academyId);

        AssessmentSkill skill = new AssessmentSkill();
        skill.setCode(code);
        skill.setName(code);
        skill.setMaxScore(maxScore);
        skill.setActive(true);
        skill.setAcademy(academy);
        return skill;
    }
}
