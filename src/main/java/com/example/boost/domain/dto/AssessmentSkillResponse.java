package com.example.boost.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class AssessmentSkillResponse {
    private UUID id;
    private UUID academyId;
    private String code;
    private String name;
    private String description;
    private Integer orderNo;
    private boolean active;
    private Integer maxScore;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
