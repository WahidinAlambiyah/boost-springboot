package com.example.boost.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PayrollResponse(
        UUID id,
        UUID academyId,
        Integer periodMonth,
        Integer periodYear,
        String status,
        OffsetDateTime generatedAt,
        OffsetDateTime approvedAt,
        OffsetDateTime paidAt,
        List<PayrollItemResponse> items
) {
}
