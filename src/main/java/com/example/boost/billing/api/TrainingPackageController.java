package com.example.boost.billing.api;

import com.example.boost.billing.application.TrainingPackageService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.TrainingPackageCreateRequest;
import com.example.boost.domain.dto.TrainingPackageResponse;
import com.example.boost.domain.dto.TrainingPackageUpdateRequest;
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
public class TrainingPackageController {
    private final TrainingPackageService trainingPackageService;

    @GetMapping
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    public ResponseEntity<ApiResponse<List<TrainingPackageResponse>>> list(
            @RequestParam UUID academyId,
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Training packages retrieved",
                trainingPackageService.list(academyId, activeOnly)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
    public ResponseEntity<ApiResponse<TrainingPackageResponse>> create(
            @Valid @RequestBody TrainingPackageCreateRequest request
    ) {
        TrainingPackageResponse response = trainingPackageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Training package created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PACKAGE_READ')")
    public ResponseEntity<ApiResponse<TrainingPackageResponse>> getById(
            @RequestParam UUID academyId,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Training package retrieved",
                trainingPackageService.getById(academyId, id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PACKAGE_WRITE')")
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
    public ResponseEntity<ApiResponse<String>> delete(
            @RequestParam UUID academyId,
            @PathVariable UUID id
    ) {
        trainingPackageService.delete(academyId, id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Training package deleted", "deleted"));
    }
}
