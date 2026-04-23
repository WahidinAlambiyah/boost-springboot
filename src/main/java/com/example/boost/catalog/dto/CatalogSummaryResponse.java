package com.example.boost.catalog.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CatalogSummaryResponse {
    String code;
    String name;
    String status;
}
