package com.example.boost.domain.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationSummaryResponse {
    String code;
    String name;
    String status;
}
