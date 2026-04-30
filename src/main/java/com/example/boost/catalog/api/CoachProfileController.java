package com.example.boost.catalog.api;

import com.example.boost.catalog.application.CoachProfileService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.CoachProfileCreateRequest;
import com.example.boost.domain.dto.CoachProfileResponse;
import com.example.boost.domain.dto.CoachProfileUpdateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/coach-profiles")
@RequiredArgsConstructor
public class CoachProfileController {
    private final CoachProfileService coachProfileService;

    @GetMapping
    @PreAuthorize("hasAuthority('COACH_READ')")
    public ResponseEntity<ApiResponse<Page<CoachProfileResponse>>> list(@RequestParam UUID academyId,
                                                                        @RequestParam(required = false) String keyword,
                                                                        Pageable pageable) {
        Page<CoachProfileResponse> responses = coachProfileService.list(academyId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach profiles retrieved", responses));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COACH_WRITE')")
    public ResponseEntity<ApiResponse<CoachProfileResponse>> create(@Valid @RequestBody CoachProfileCreateRequest request) {
        CoachProfileResponse response = coachProfileService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Coach profile created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COACH_READ')")
    public ResponseEntity<ApiResponse<CoachProfileResponse>> getById(@PathVariable UUID id) {
        CoachProfileResponse response = coachProfileService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach profile retrieved", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COACH_WRITE')")
    public ResponseEntity<ApiResponse<CoachProfileResponse>> update(@PathVariable UUID id,
                                                                    @Valid @RequestBody CoachProfileUpdateRequest request) {
        CoachProfileResponse response = coachProfileService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Coach profile updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COACH_WRITE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        coachProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
