package com.example.boost.domain.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionAttendanceStudentResponse(
        UUID studentId,
        String studentName,
        String attendanceStatus,
        Instant checkInAt,
        String remarks
) {
}
