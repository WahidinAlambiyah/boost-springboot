package com.example.boost.domain.dto;

import java.util.UUID;

public record SkillResponse(
        UUID id,
        String code,
        String name,
        String description,
        Integer orderNo,
        boolean active,
        Integer maxScore
) {
}
