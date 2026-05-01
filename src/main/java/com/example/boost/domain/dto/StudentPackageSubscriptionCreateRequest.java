package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record StudentPackageSubscriptionCreateRequest(
        @NotNull UUID packageId,
        @NotNull LocalDate startDate,
        LocalDate endDate
) {
}
