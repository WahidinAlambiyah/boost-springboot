package com.example.boost.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        @NotNull UUID invoiceId,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {
}
