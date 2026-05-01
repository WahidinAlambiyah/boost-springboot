package com.example.boost.billing.api;

import com.example.boost.billing.application.StudentPackageSubscriptionService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.StudentPackageSubscriptionCreateRequest;
import com.example.boost.domain.dto.StudentPackageSubscriptionResponse;
import com.example.boost.domain.dto.StudentPackageSubscriptionUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Package Management", description = "Requires PACKAGE_READ/PACKAGE_WRITE permissions")
public class StudentPackageSubscriptionController {
    private final StudentPackageSubscriptionService subscriptionService;

    @GetMapping
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    @Operation(summary = "List student package subscriptions")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student package subscriptions retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<StudentPackageSubscriptionResponse>>> list(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Student package subscriptions retrieved",
                subscriptionService.listByStudent(studentId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    @Operation(summary = "Create student package subscription")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Student package subscription created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<StudentPackageSubscriptionResponse>> create(
            @PathVariable UUID studentId,
            @Valid @RequestBody StudentPackageSubscriptionCreateRequest request
    ) {
        StudentPackageSubscriptionResponse response = subscriptionService.create(studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Student package subscription created", response));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    @Operation(summary = "Update student package subscription")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student package subscription updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<StudentPackageSubscriptionResponse>> update(
            @PathVariable UUID studentId,
            @RequestParam UUID subscriptionId,
            @Valid @RequestBody StudentPackageSubscriptionUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Student package subscription updated",
                subscriptionService.update(studentId, subscriptionId, request)));
    }
}
