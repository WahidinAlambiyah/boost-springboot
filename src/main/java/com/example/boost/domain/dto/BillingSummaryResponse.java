package com.example.boost.domain.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BillingSummaryResponse {
    String code;
    String name;
    String status;
}
