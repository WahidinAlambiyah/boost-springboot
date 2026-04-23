package com.example.boost.attendance.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AttendanceSummaryResponse {
    String code;
    String name;
    String status;
}
