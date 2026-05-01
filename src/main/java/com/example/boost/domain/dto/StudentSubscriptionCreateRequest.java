package com.example.boost.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record StudentSubscriptionCreateRequest(
        @NotNull UUID academyId,
        @NotNull UUID studentId,
        @NotNull UUID packageId,
        String status,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @Min(0) Integer remainingSessions
) {
}
