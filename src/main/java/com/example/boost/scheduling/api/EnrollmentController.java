package com.example.boost.scheduling.api;

import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.domain.dto.EnrollmentActionResponse;
import com.example.boost.domain.dto.EnrollmentRegisterRequest;
import com.example.boost.domain.dto.EnrollmentSummaryResponse;
import com.example.boost.scheduling.application.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('ENROLLMENT_WRITE')")
    public ResponseEntity<ApiResponse<EnrollmentActionResponse>> register(
            @Valid @RequestBody EnrollmentRegisterRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        EnrollmentActionResponse response = enrollmentService.register(request.studentId(), request.classGroupId(), idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Enrollment processed", response));
    }

    @PostMapping("/{enrollmentId}/cancel")
    @PreAuthorize("hasAuthority('ENROLLMENT_WRITE')")
    public ResponseEntity<ApiResponse<EnrollmentActionResponse>> cancel(@PathVariable UUID enrollmentId) {
        EnrollmentActionResponse response = enrollmentService.cancelAndPromote(enrollmentId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Enrollment cancelled and waitlist promoted", response));
    }
}
