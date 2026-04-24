package com.example.boost.billing.api;

import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.BillingSummaryResponse;
import com.example.boost.domain.dto.PaymentRequest;
import com.example.boost.domain.dto.PaymentResponse;
import com.example.boost.billing.application.BillingService;
import com.example.boost.common.util.HeaderUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {
    private final BillingService billingService;

    @GetMapping
    @PreAuthorize("hasAuthority('BILLING_READ')")
    public ResponseEntity<ApiResponse<List<BillingSummaryResponse>>> getSummaries() {
        List<BillingSummaryResponse> responses = billingService.getSummaries();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Billing records retrieved", responses));
    }

    @GetMapping("/summary-count")
    @PreAuthorize("hasAuthority('BILLING_WRITE')")
    public ResponseEntity<ApiResponse<Long>> getWritableSummaryCount() {
        long count = billingService.getWritableSummaryCount();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Billing writable summary count retrieved", count));
    }

    @PostMapping("/pay")
    @PreAuthorize("hasAuthority('BILLING_WRITE')")
    public ResponseEntity<ApiResponse<PaymentResponse>> pay(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        PaymentResponse response = billingService.pay(request, HeaderUtils.normalize(idempotencyKey));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Payment processed", response));
    }
}
