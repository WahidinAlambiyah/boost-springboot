package com.example.boost.billing.api;

import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.BillingSummaryResponse;
import com.example.boost.domain.dto.PaymentRequest;
import com.example.boost.domain.dto.PaymentResponse;
import com.example.boost.billing.application.BillingService;
import com.example.boost.common.util.HeaderUtils;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "11. Billing")
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {
    private final BillingService billingService;

    @GetMapping
    @PreAuthorize("hasAuthority('BILLING_READ')")
    @Operation(
            summary = "List billing summaries",
            description = "Mengambil ringkasan billing. Authority utama: BILLING_READ."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Billing berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority BILLING_READ")
    })
    public ResponseEntity<ApiResponse<List<BillingSummaryResponse>>> getSummaries() {
        List<BillingSummaryResponse> responses = billingService.getSummaries();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Billing records retrieved", responses));
    }

    @GetMapping("/summary-count")
    @PreAuthorize("hasAuthority('BILLING_WRITE')")
    @Operation(
            summary = "Get writable billing summary count",
            description = "Mengambil jumlah billing yang bisa dikelola. Authority utama: BILLING_WRITE."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jumlah billing berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority BILLING_WRITE")
    })
    public ResponseEntity<ApiResponse<Long>> getWritableSummaryCount() {
        long count = billingService.getWritableSummaryCount();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Billing writable summary count retrieved", count));
    }

    @PostMapping("/pay")
    @PreAuthorize("hasAuthority('BILLING_WRITE')")
    @Operation(
            summary = "Create payment",
            description = "Memproses pembayaran dengan idempotency key. Authority utama: BILLING_WRITE."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Pembayaran berhasil diproses"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority BILLING_WRITE"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validasi payload gagal")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> pay(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        PaymentResponse response = billingService.pay(request, HeaderUtils.normalize(idempotencyKey));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Payment processed", response));
    }
}
