package com.example.boost.service;

import com.example.boost.domain.dto.BillingSummaryResponse;
import com.example.boost.repository.BillingRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService {
    private final BillingRepository billingRepository;

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
}
