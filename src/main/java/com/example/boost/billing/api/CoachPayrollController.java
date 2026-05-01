package com.example.boost.billing.api;

import com.example.boost.billing.application.CoachPayrollService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payroll/coach")
@RequiredArgsConstructor
public class CoachPayrollController {
    private final CoachPayrollService coachPayrollService;

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('PAYROLL_WRITE')")
    public ResponseEntity<ApiResponse<PayrollResponse>> generate(@Valid @RequestBody PayrollGenerateRequest request) {
        PayrollResponse response = coachPayrollService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(), "Coach payroll generated", response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> findByMonthAndYear(@RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payrolls fetched", coachPayrollService.findByMonthAndYear(month, year)));
    }

    @GetMapping("/{payrollPeriodId}")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    public ResponseEntity<ApiResponse<PayrollResponse>> findById(@PathVariable UUID payrollPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payroll fetched", coachPayrollService.findById(payrollPeriodId)));
    }

    @PostMapping("/{payrollPeriodId}/approve")
    @PreAuthorize("hasAuthority('PAYROLL_WRITE')")
    public ResponseEntity<ApiResponse<PayrollResponse>> approve(@PathVariable UUID payrollPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payroll approved", coachPayrollService.approve(payrollPeriodId)));
    }

    @PostMapping("/{payrollPeriodId}/mark-paid")
    @PreAuthorize("hasAuthority('PAYROLL_WRITE')")
    public ResponseEntity<ApiResponse<PayrollResponse>> markPaid(@PathVariable UUID payrollPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payroll marked paid", coachPayrollService.markPaid(payrollPeriodId)));
    }

    @PutMapping("/{payrollPeriodId}/items/{itemId}")
    @PreAuthorize("hasAuthority('PAYROLL_WRITE')")
    public ResponseEntity<ApiResponse<PayrollItemResponse>> updateItem(@PathVariable UUID payrollPeriodId, @PathVariable UUID itemId, @Valid @RequestBody PayrollItemUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payroll item updated", coachPayrollService.updateItem(payrollPeriodId, itemId, request)));
    }
}
