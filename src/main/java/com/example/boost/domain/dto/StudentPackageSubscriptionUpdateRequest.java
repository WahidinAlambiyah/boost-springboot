package com.example.boost.domain.dto;

import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public record StudentPackageSubscriptionUpdateRequest(
        String status,
        LocalDate endDate,
        @Min(0) Integer remainingSessions
) {
}
