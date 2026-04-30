package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record AttendanceUpsertRequest(
        @NotBlank(message = "attendanceStatus is required")
        String attendanceStatus,
        Instant checkInAt,
        String remarks
) {
}
