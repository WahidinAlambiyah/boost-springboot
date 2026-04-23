package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnrollmentRegisterRequest(
        @NotNull UUID studentId,
        @NotNull UUID classGroupId
) {
}
