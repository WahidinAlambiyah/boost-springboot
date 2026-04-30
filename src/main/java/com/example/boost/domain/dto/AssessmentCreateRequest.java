package com.example.boost.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AssessmentCreateRequest(
        @NotNull(message = "classSessionId is required")
        UUID classSessionId,

        @NotNull(message = "studentId is required")
        UUID studentId,

        @NotNull(message = "coachId is required")
        UUID coachId,

        @NotNull(message = "assessedAt is required")
        OffsetDateTime assessedAt,

        String notes,

        @NotEmpty(message = "scores must not be empty")
        @Valid
        List<AssessmentScoreRequest> scores
) {
}
