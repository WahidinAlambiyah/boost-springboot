package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.util.UUID;

public record AttendanceSubmitRequest(
        @NotNull UUID classGroupId,
        @NotNull LocalDate sessionDate,
        @PositiveOrZero int presentCount,
        @PositiveOrZero int absentCount
) {
}
