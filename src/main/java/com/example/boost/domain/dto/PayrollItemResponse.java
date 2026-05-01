package com.example.boost.domain.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PayrollItemResponse(
        UUID id,
        UUID coachId,
        BigDecimal baseAmount,
        BigDecimal bonusAmount,
        BigDecimal deductionAmount,
        BigDecimal totalAmount
) {
}
