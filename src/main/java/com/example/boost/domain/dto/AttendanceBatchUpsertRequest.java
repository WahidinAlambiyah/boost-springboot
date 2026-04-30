package com.example.boost.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AttendanceBatchUpsertRequest(
        @NotEmpty(message = "records is required")
        List<@Valid AttendanceBatchUpsertRecordRequest> records
) {
}
