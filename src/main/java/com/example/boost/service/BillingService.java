package com.example.boost.service;

import com.example.boost.domain.dto.BillingSummaryResponse;
import com.example.boost.domain.dto.PaymentRequest;
import com.example.boost.domain.dto.PaymentResponse;
import com.example.boost.domain.entity.Payment;
import com.example.boost.domain.entity.PaymentStatus;
import com.example.boost.exception.BadRequestException;
import com.example.boost.repository.BillingRepository;
import com.example.boost.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillingService {
    private static final String PAYMENT_SCOPE = "BILLING_PAY";

    private final BillingRepository billingRepository;
    private final PaymentJpaRepository paymentJpaRepository;
    private final IdempotencyService idempotencyService;
    private final OutboxEventService outboxEventService;
    private final DomainMetricsService domainMetricsService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('BILLING_READ')")
    public List<BillingSummaryResponse> getSummaries() {
        return billingRepository.findSummaries();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('BILLING_WRITE')")
    public long getWritableSummaryCount() {
        return billingRepository.findSummaries().size();
    }

    @Transactional
    @PreAuthorize("hasAuthority('BILLING_WRITE')")
    public PaymentResponse pay(PaymentRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BadRequestException("Idempotency-Key header is required");
        }

        String scope = PAYMENT_SCOPE + ":" + request.invoiceId();
        Optional<PaymentResponse> cached = idempotencyService.getStoredResponse(scope, idempotencyKey, PaymentResponse.class);
        if (cached.isPresent()) {
            return cached.get();
        }

        Payment payment = new Payment();
        payment.setInvoiceId(request.invoiceId());
        payment.setAmount(request.amount());
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus(PaymentStatus.SUCCESS);
        Payment saved = paymentJpaRepository.save(payment);

        if (saved.getStatus() == PaymentStatus.SUCCESS) {
            domainMetricsService.recordPaymentSuccess();
        } else {
            domainMetricsService.recordPaymentFailure();
        }

        outboxEventService.append(
                "PAYMENT",
                saved.getId(),
                "payment.settled",
                Map.of(
                        "invoiceId", saved.getInvoiceId(),
                        "amount", saved.getAmount(),
                        "status", saved.getStatus().name()
                )
        );

        PaymentResponse response = new PaymentResponse(saved.getId(), saved.getInvoiceId(), saved.getAmount(), saved.getStatus().name());
        idempotencyService.saveResponse(scope, idempotencyKey, response);
        return response;
    }
}
