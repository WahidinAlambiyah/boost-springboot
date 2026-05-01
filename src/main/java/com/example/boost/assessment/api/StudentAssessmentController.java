package com.example.boost.assessment.api;

import com.example.boost.assessment.application.StudentAssessmentService;
import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.AssessmentCreateRequest;
import com.example.boost.domain.dto.AssessmentDetailResponse;
import com.example.boost.domain.dto.AssessmentUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "14. Assessment")
public class StudentAssessmentController {
    private final StudentAssessmentService studentAssessmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('ASSESSMENT_READ')")
    @Operation(summary = "List assessments")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assessments retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
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
    @Operation(summary = "Create assessment")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Assessment created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AssessmentDetailResponse>> create(@RequestParam UUID academyId,
                                                                        @Valid @RequestBody AssessmentCreateRequest request) {
        AssessmentDetailResponse response = studentAssessmentService.create(academyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Assessment created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSESSMENT_READ')")
    @Operation(summary = "Get assessment by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assessment retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AssessmentDetailResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Assessment retrieved",
                studentAssessmentService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE')")
    @Operation(summary = "Update assessment")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assessment updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<AssessmentDetailResponse>> update(@PathVariable UUID id,
                                                                        @RequestParam UUID academyId,
                                                                        @Valid @RequestBody AssessmentUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Assessment updated",
                studentAssessmentService.update(id, academyId, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE')")
    @Operation(summary = "Delete assessment")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assessment deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {
        studentAssessmentService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Assessment deleted", "deleted"));
    }
}
