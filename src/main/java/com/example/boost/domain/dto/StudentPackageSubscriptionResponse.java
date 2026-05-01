package com.example.boost.domain.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record StudentPackageSubscriptionResponse(
        UUID id,
        UUID academyId,
        UUID studentId,
        UUID packageId,
        String packageType,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        Integer remainingSessions,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
