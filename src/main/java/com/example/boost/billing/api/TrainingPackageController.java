package com.example.boost.billing.api;

import com.example.boost.billing.application.TrainingPackageService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.TrainingPackageCreateRequest;
import com.example.boost.domain.dto.TrainingPackageResponse;
import com.example.boost.domain.dto.TrainingPackageUpdateRequest;
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
@RequestMapping("/api/training-packages")
@RequiredArgsConstructor
@Tag(name = "Package Management", description = "Requires PACKAGE_READ/PACKAGE_WRITE permissions")
public class TrainingPackageController {
    private final TrainingPackageService trainingPackageService;

    @GetMapping
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    @Operation(summary = "List training packages")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Training packages retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<TrainingPackageResponse>>> list(
            @RequestParam UUID academyId,
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Training packages retrieved",
                trainingPackageService.list(academyId, activeOnly)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    @Operation(summary = "Create training package")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Training package created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<TrainingPackageResponse>> create(
            @Valid @RequestBody TrainingPackageCreateRequest request
    ) {
        TrainingPackageResponse response = trainingPackageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Training package created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    @Operation(summary = "Get training package by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Training package retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<TrainingPackageResponse>> getById(
            @RequestParam UUID academyId,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Training package retrieved",
                trainingPackageService.getById(academyId, id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    @Operation(summary = "Update training package")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Training package updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<TrainingPackageResponse>> update(
            @RequestParam UUID academyId,
            @PathVariable UUID id,
            @Valid @RequestBody TrainingPackageUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Training package updated",
                trainingPackageService.update(academyId, id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    @Operation(summary = "Delete training package")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Training package deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<String>> delete(
            @RequestParam UUID academyId,
            @PathVariable UUID id
    ) {
        trainingPackageService.delete(academyId, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT.value(), "Training package deleted", null));
    }
}
