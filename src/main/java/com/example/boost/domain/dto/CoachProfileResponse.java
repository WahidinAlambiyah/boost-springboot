package com.example.boost.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class CoachProfileResponse {
    private UUID id;
    private UUID academyId;
    private UUID userId;
    private String coachNo;
    private String fullName;
    private String phone;
    private String specialties;
    private String bio;
    private String employmentType;
    private String payType;
    private boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
