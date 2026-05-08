package com.example.boost.domain.dto;

import java.util.Map;

public record LookupOptionResponse(
        String id,
        String code,
        String label,
        String description,
        Map<String, Object> metadata
) {
}
