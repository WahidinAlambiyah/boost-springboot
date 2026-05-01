package com.example.boost.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PayrollItemUpdateRequest(
        @NotNull @DecimalMin(value = "0.00") BigDecimal bonusAmount,
        @NotNull @DecimalMin(value = "0.00") BigDecimal deductionAmount
) {
}
