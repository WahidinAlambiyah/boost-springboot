package com.example.boost.assessment.application;

import com.example.boost.assessment.infrastructure.AssessmentSkillRepository;
import com.example.boost.common.exception.BadRequestException;
import com.example.boost.domain.dto.AssessmentScoreRequest;
import com.example.boost.domain.entity.AssessmentSkill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AssessmentRequestValidator {
    private final AssessmentSkillRepository assessmentSkillRepository;

    public Map<String, AssessmentSkill> validateAndResolveScores(List<AssessmentScoreRequest> scores, java.util.UUID academyId) {
        if (scores == null || scores.isEmpty()) {
            throw new BadRequestException("scores must not be empty");
        }

        Set<String> duplicateCheck = new HashSet<>();
        for (AssessmentScoreRequest score : scores) {
            String normalizedCode = score.skillCode().trim().toLowerCase(Locale.ROOT);
            if (!duplicateCheck.add(normalizedCode)) {
                throw new BadRequestException("skillCode must be unique in one payload: " + score.skillCode());
            }
        }

        Set<String> skillCodes = scores.stream().map(s -> s.skillCode().trim()).collect(Collectors.toSet());
        List<AssessmentSkill> skills = assessmentSkillRepository.findByCodeInAndActiveTrue(skillCodes);
        Map<String, AssessmentSkill> byCode = new HashMap<>();
        for (AssessmentSkill skill : skills) {
            if (skill.getAcademy() != null && !academyId.equals(skill.getAcademy().getId())) {
                continue;
            }
            byCode.put(skill.getCode().toLowerCase(Locale.ROOT), skill);
        }

        for (AssessmentScoreRequest score : scores) {
            AssessmentSkill skill = byCode.get(score.skillCode().trim().toLowerCase(Locale.ROOT));
            if (skill == null) {
                throw new BadRequestException("Unknown skillCode: " + score.skillCode());
            }
            if (score.score().compareTo(BigDecimal.ONE) < 0) {
                throw new BadRequestException("score must be at least 1 for skillCode " + score.skillCode());
            }
            BigDecimal max = BigDecimal.valueOf(skill.getMaxScore());
            if (score.score().compareTo(max) > 0) {
                throw new BadRequestException("score exceeds max_score for skillCode " + score.skillCode());
            }
        }

        return byCode;
    }
}
