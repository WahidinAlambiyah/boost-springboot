package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record SessionAttendanceStudentUpsertRequest(
        @NotNull(message = "studentId is required")
        UUID studentId,
        @NotBlank(message = "attendanceStatus is required")
        String attendanceStatus,
        Instant checkInAt,
        String remarks
) {
}
