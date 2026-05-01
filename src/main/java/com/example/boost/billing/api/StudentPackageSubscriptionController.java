package com.example.boost.billing.api;

import com.example.boost.billing.application.StudentPackageSubscriptionService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.StudentPackageSubscriptionCreateRequest;
import com.example.boost.domain.dto.StudentPackageSubscriptionResponse;
import com.example.boost.domain.dto.StudentPackageSubscriptionUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students/{studentId}/packages")
@RequiredArgsConstructor
public class StudentPackageSubscriptionController {
    private final StudentPackageSubscriptionService subscriptionService;

    @GetMapping
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    public ResponseEntity<ApiResponse<List<StudentPackageSubscriptionResponse>>> list(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Student package subscriptions retrieved",
                subscriptionService.listByStudent(studentId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    public ResponseEntity<ApiResponse<StudentPackageSubscriptionResponse>> create(
            @PathVariable UUID studentId,
            @Valid @RequestBody StudentPackageSubscriptionCreateRequest request
    ) {
        StudentPackageSubscriptionResponse response = subscriptionService.create(studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Student package subscription created", response));
    }

    @PutMapping("/{subscriptionId}")
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    public ResponseEntity<ApiResponse<StudentPackageSubscriptionResponse>> update(
            @PathVariable UUID studentId,
            @PathVariable UUID subscriptionId,
            @Valid @RequestBody StudentPackageSubscriptionUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Student package subscription updated",
                subscriptionService.update(studentId, subscriptionId, request)));
    }
}
