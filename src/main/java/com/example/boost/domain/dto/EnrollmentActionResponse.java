package com.example.boost.domain.dto;

import java.util.UUID;

public record EnrollmentActionResponse(
        UUID enrollmentId,
        String status,
        Integer waitlistPosition
) {
}
