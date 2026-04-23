package com.example.boost.billing.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BillingSummaryResponse {
    String code;
    String name;
    String status;
}
