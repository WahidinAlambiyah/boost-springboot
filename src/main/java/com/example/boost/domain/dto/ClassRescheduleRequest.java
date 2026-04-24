package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClassRescheduleRequest(
        @NotNull UUID classGroupId,
        @NotNull OffsetDateTime previousStartAt,
        @NotNull OffsetDateTime newStartAt,
        String reason
) {
}
