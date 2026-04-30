package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CoachProfileCreateRequest {
    @NotNull
    private UUID academyId;

    @NotNull
    private UUID userId;

    @Size(max = 50)
    private String coachNo;

    @NotBlank
    @Size(min = 3, max = 255)
    private String fullName;

    @Size(max = 30)
    private String phone;

    @Size(max = 2_000)
    private String specialties;

    @Size(max = 2_000)
    private String bio;

    @NotBlank
    @Pattern(regexp = "FULL_TIME|PART_TIME|CONTRACT", message = "employmentType must be one of: FULL_TIME, PART_TIME, CONTRACT")
    private String employmentType;

    @NotBlank
    @Pattern(regexp = "SALARY|HOURLY|PER_SESSION", message = "payType must be one of: SALARY, HOURLY, PER_SESSION")
    private String payType;
}
