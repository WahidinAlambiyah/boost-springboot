package com.example.boost.catalog.api;

import com.example.boost.catalog.application.AcademyService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.AcademyCreateRequest;
import com.example.boost.domain.dto.AcademyResponse;
import com.example.boost.domain.dto.AcademyUpdateRequest;

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
@RequestMapping("/api/academies")
@RequiredArgsConstructor
public class AcademyController {
    private final AcademyService academyService;

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMY_READ')")
    public ResponseEntity<ApiResponse<Page<AcademyResponse>>> list(@RequestParam(required = false) String keyword,
                                                                   Pageable pageable) {
        Page<AcademyResponse> responses = academyService.list(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Academies retrieved", responses));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACADEMY_WRITE')")
    public ResponseEntity<ApiResponse<AcademyResponse>> create(@Valid @RequestBody AcademyCreateRequest request) {
        AcademyResponse response = academyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Academy created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMY_READ')")
    public ResponseEntity<ApiResponse<AcademyResponse>> getById(@PathVariable UUID id) {
        AcademyResponse response = academyService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Academy retrieved", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMY_WRITE')")
    public ResponseEntity<ApiResponse<AcademyResponse>> update(@PathVariable UUID id,
                                                               @Valid @RequestBody AcademyUpdateRequest request) {
        AcademyResponse response = academyService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Academy updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMY_WRITE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        academyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
