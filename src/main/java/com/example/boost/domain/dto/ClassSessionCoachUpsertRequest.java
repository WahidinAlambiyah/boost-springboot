package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClassSessionCoachUpsertRequest(
        @NotNull UUID coachId,
        @Size(max = 50) String coachRole,
        @Size(max = 30) String attendanceStatus,
        OffsetDateTime checkInAt,
        OffsetDateTime checkOutAt,
        @Size(max = 2000) String notes
) {
}
