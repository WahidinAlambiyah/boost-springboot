package com.example.boost.scheduling.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SchedulingSummaryResponse {
    String code;
    String name;
    String status;
}
