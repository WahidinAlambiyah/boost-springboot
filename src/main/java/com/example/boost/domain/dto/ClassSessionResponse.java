package com.example.boost.domain.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ClassSessionResponse(
        UUID id,
        UUID classGroupId,
        UUID academyId,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        UUID locationId,
        String status,
        List<UUID> coachIds,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
