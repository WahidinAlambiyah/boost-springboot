package com.example.boost.enrollment.controller;

import com.example.boost.enrollment.dto.EnrollmentSummaryResponse;
import com.example.boost.enrollment.service.EnrollmentService;
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
@RequestMapping("/api/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('ENROLLMENT_READ')")
    public ResponseEntity<ApiResponse<List<EnrollmentSummaryResponse>>> getSummaries() {
        List<EnrollmentSummaryResponse> responses = enrollmentService.getSummaries();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Enrollments retrieved", responses));
    }

    @GetMapping("/summary-count")
    @PreAuthorize("hasAuthority('ENROLLMENT_WRITE')")
    public ResponseEntity<ApiResponse<Long>> getWritableSummaryCount() {
        long count = enrollmentService.getWritableSummaryCount();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Enrollment writable summary count retrieved", count));
    }
}
