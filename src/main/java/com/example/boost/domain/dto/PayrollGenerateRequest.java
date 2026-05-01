package com.example.boost.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PayrollGenerateRequest(
        @NotNull UUID academyId,
        @NotNull @Min(1) @Max(12) Integer periodMonth,
        @NotNull @Min(2000) Integer periodYear
) {
}
