package com.example.boost.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AssessmentScoreRequest(
        @NotBlank(message = "skillCode is required")
        String skillCode,

        @NotNull(message = "score is required")
        @DecimalMin(value = "1.0", message = "score must be at least 1")
        BigDecimal score,

        String notes
) {
}
