package com.example.boost.billing.api;

import com.example.boost.billing.application.CoachPayrollService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payroll/coach")
@RequiredArgsConstructor
public class CoachPayrollController {
    private final CoachPayrollService coachPayrollService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<PayrollResponse>> generate(@Valid @RequestBody PayrollGenerateRequest request) {
        PayrollResponse response = coachPayrollService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(), "Coach payroll generated", response));
    }

    @PostMapping("/{payrollPeriodId}/approve")
    public ResponseEntity<ApiResponse<PayrollResponse>> approve(@PathVariable UUID payrollPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payroll approved", coachPayrollService.approve(payrollPeriodId)));
    }

    @PostMapping("/{payrollPeriodId}/mark-paid")
    public ResponseEntity<ApiResponse<PayrollResponse>> markPaid(@PathVariable UUID payrollPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payroll marked paid", coachPayrollService.markPaid(payrollPeriodId)));
    }

    @PutMapping("/{payrollPeriodId}/items/{itemId}")
    public ResponseEntity<ApiResponse<PayrollItemResponse>> updateItem(@PathVariable UUID payrollPeriodId, @PathVariable UUID itemId, @Valid @RequestBody PayrollItemUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payroll item updated", coachPayrollService.updateItem(payrollPeriodId, itemId, request)));
    }
}
