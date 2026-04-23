package com.example.boost.controller;

import com.example.boost.domain.dto.BillingSummaryResponse;
import com.example.boost.service.BillingService;
import com.example.boost.domain.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
