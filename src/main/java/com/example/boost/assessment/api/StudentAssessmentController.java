package com.example.boost.assessment.api;

import com.example.boost.assessment.application.StudentAssessmentService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.AssessmentCreateRequest;
import com.example.boost.domain.dto.AssessmentDetailResponse;
import com.example.boost.domain.dto.AssessmentUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class StudentAssessmentController {
    private final StudentAssessmentService studentAssessmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('ASSESSMENT_READ')")
    public ResponseEntity<ApiResponse<List<AssessmentDetailResponse>>> list(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID classSessionId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Assessments retrieved",
                studentAssessmentService.list(studentId, classSessionId, from, to)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE')")
    public ResponseEntity<ApiResponse<AssessmentDetailResponse>> create(@RequestParam UUID academyId,
                                                                        @Valid @RequestBody AssessmentCreateRequest request) {
        AssessmentDetailResponse response = studentAssessmentService.create(academyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Assessment created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSESSMENT_READ')")
    public ResponseEntity<ApiResponse<AssessmentDetailResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Assessment retrieved",
                studentAssessmentService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE')")
    public ResponseEntity<ApiResponse<AssessmentDetailResponse>> update(@PathVariable UUID id,
                                                                        @RequestParam UUID academyId,
                                                                        @Valid @RequestBody AssessmentUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Assessment updated",
                studentAssessmentService.update(id, academyId, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE')")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {
        studentAssessmentService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Assessment deleted", "deleted"));
    }
}
