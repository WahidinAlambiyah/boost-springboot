package com.example.boost.domain.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TrainingPackageResponse(
        UUID id,
        UUID academyId,
        String code,
        String name,
        String packageType,
        BigDecimal price,
        Integer sessionQuota,
        String description,
        Boolean isActive,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
