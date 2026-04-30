package com.example.boost.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AssessmentDetailResponse(
        UUID id,
        UUID classSessionId,
        UUID studentId,
        UUID coachId,
        OffsetDateTime assessedAt,
        String notes,
        List<AssessmentScoreDetailResponse> scores
) {
    public record AssessmentScoreDetailResponse(
            UUID skillId,
            String skillCode,
            String skillName,
            Integer maxScore,
            String score,
            String notes
    ) {
    }
}
