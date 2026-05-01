package com.example.boost.domain.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record TrainingPackageCreateRequest(
        @NotNull UUID academyId,
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank
        @Pattern(regexp = "TRIAL|PER_SESSION|MONTHLY|SESSION_BUNDLE")
        String packageType,
        @NotNull
        @DecimalMin(value = "0.0")
        BigDecimal price,
        Integer sessionQuota,
        String description,
        Boolean isActive
) {
    @AssertTrue(message = "session_quota wajib untuk SESSION_BUNDLE")
    public boolean isSessionQuotaValid() {
        return !"SESSION_BUNDLE".equals(packageType) || sessionQuota != null;
    }
}
