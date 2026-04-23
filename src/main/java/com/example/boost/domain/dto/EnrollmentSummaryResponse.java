package com.example.boost.domain.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EnrollmentSummaryResponse {
    String code;
    String name;
    String status;
}
