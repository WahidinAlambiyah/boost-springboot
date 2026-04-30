package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ClassSessionCrudRequest(
        @NotNull UUID classGroupId,
        @NotNull LocalDate sessionDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        UUID locationId,
        List<UUID> coachIds,
        String status
) {
}
