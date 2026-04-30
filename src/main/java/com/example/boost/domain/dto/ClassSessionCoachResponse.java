package com.example.boost.domain.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClassSessionCoachResponse(
        UUID id,
        UUID classSessionId,
        UUID academyId,
        UUID coachId,
        String coachRole,
        String attendanceStatus,
        OffsetDateTime checkInAt,
        OffsetDateTime checkOutAt,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
