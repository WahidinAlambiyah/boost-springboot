package com.example.boost.billing.repository;

import com.example.boost.billing.dto.BillingSummaryResponse;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BillingRepository {
    public List<BillingSummaryResponse> findSummaries() {
        return List.of(
                BillingSummaryResponse.builder()
                        .code("BILLING-001")
                        .name("Default Billing Record")
                        .status("ACTIVE")
                        .build()
        );
    }
}
