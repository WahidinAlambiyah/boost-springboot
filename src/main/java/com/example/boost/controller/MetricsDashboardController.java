package com.example.boost.controller;

import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.service.DomainMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsDashboardController {
    private final DomainMetricsService domainMetricsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ENROLLMENT_READ','BILLING_READ','SCHEDULE_READ','NOTIFICATION_READ')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Domain metrics dashboard retrieved",
                domainMetricsService.snapshot()
        ));
    }
}
