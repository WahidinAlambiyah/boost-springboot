package com.example.boost.assessment.api;

import com.example.boost.assessment.application.AssessmentSkillService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.AssessmentSkillCreateRequest;
import com.example.boost.domain.dto.AssessmentSkillResponse;
import com.example.boost.domain.dto.AssessmentSkillUpdateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/assessment-skills")
@RequiredArgsConstructor
@Tag(name = "Assessment Skills")
public class AssessmentSkillController {
    private final AssessmentSkillService assessmentSkillService;

    @GetMapping
    @PreAuthorize("hasAuthority('ASSESSMENT_READ')")
    public ResponseEntity<ApiResponse<Page<AssessmentSkillResponse>>> list(@RequestParam(required = false) UUID academyId,
                                                                            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Assessment skills retrieved",
                assessmentSkillService.list(academyId, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE')")
    public ResponseEntity<ApiResponse<AssessmentSkillResponse>> create(@Valid @RequestBody AssessmentSkillCreateRequest request) {
        AssessmentSkillResponse response = assessmentSkillService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Assessment skill created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSESSMENT_READ')")
    public ResponseEntity<ApiResponse<AssessmentSkillResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Assessment skill retrieved",
                assessmentSkillService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE')")
    public ResponseEntity<ApiResponse<AssessmentSkillResponse>> update(@PathVariable UUID id,
                                                                        @Valid @RequestBody AssessmentSkillUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Assessment skill updated",
                assessmentSkillService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE')")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {
        assessmentSkillService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Assessment skill deleted", "deleted"));
    }
}
