package com.example.boost.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AttendanceBatchUpsertRecordRequest(
        @NotNull(message = "studentId is required")
        UUID studentId,
        @NotNull(message = "attendance is required")
        @Valid
        AttendanceUpsertRequest attendance
) {
}
