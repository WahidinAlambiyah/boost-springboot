package com.example.boost.billing.api;

import com.example.boost.billing.application.CoachPayrollService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Coach Payroll", description = "Requires PAYROLL_READ/PAYROLL_WRITE permissions")
public class CoachPayrollController {
    private final CoachPayrollService coachPayrollService;

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('PAYROLL_WRITE')")
    @Operation(summary = "Generate coach payroll")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Coach payroll generated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PayrollResponse>> generate(@Valid @RequestBody PayrollGenerateRequest request) {
        PayrollResponse response = coachPayrollService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(), "Coach payroll generated", response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    @Operation(summary = "List coach payroll by month and year")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coach payrolls fetched"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> findByMonthAndYear(@RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payrolls fetched", coachPayrollService.findByMonthAndYear(month, year)));
    }

    @GetMapping("/{payrollPeriodId}")
    @PreAuthorize("hasAuthority('PAYROLL_READ')")
    @Operation(summary = "Get coach payroll by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coach payroll fetched"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PayrollResponse>> findById(@PathVariable UUID payrollPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payroll fetched", coachPayrollService.findById(payrollPeriodId)));
    }

    @PostMapping("/{payrollPeriodId}/approve")
    @PreAuthorize("hasAuthority('PAYROLL_WRITE')")
    @Operation(summary = "Approve coach payroll")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coach payroll approved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PayrollResponse>> approve(@PathVariable UUID payrollPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payroll approved", coachPayrollService.approve(payrollPeriodId)));
    }

    @PostMapping("/{payrollPeriodId}/mark-paid")
    @PreAuthorize("hasAuthority('PAYROLL_WRITE')")
    @Operation(summary = "Mark coach payroll as paid")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coach payroll marked paid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PayrollResponse>> markPaid(@PathVariable UUID payrollPeriodId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payroll marked paid", coachPayrollService.markPaid(payrollPeriodId)));
    }

    @PutMapping("/{payrollPeriodId}/items/{itemId}")
    @PreAuthorize("hasAuthority('PAYROLL_WRITE')")
    @Operation(summary = "Update coach payroll item")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Coach payroll item updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PayrollItemResponse>> updateItem(@PathVariable UUID payrollPeriodId, @PathVariable UUID itemId, @Valid @RequestBody PayrollItemUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach payroll item updated", coachPayrollService.updateItem(payrollPeriodId, itemId, request)));
    }
}
