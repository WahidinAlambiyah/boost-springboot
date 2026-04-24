package com.example.boost.domain.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID invoiceId,
        BigDecimal amount,
        String status
) {
}
