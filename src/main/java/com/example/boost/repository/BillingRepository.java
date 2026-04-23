package com.example.boost.repository;

import com.example.boost.domain.dto.BillingSummaryResponse;

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
