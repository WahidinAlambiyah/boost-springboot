package com.example.boost.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkillCrudRequest(
        @NotBlank(message = "code is required")
        @Size(max = 50, message = "code max 50 characters")
        String code,

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name max 150 characters")
        String name,

        String description,

        Integer orderNo,

        @NotNull(message = "active is required")
        Boolean active,

        @NotNull(message = "maxScore is required")
        @Min(value = 1, message = "maxScore must be at least 1")
        @Max(value = 100, message = "maxScore must be at most 100")
        Integer maxScore
) {
}
