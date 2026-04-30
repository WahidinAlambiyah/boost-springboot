package com.example.boost.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssessmentSkillUpdateRequest {
    private UUID academyId;

    @NotBlank
    @Size(max = 150)
    private String name;

    private String description;

    private Integer orderNo;

    private boolean active = true;

    @Min(1)
    @Max(100)
    private Integer maxScore = 5;
}
