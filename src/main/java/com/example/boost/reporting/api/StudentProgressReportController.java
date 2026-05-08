package com.example.boost.reporting.api;

import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.StudentProgressReportResponse;
import com.example.boost.reporting.application.StudentProgressReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/students")
@RequiredArgsConstructor
@Tag(name = "15. Reporting")
public class StudentProgressReportController {
    private final StudentProgressReportService studentProgressReportService;

    @GetMapping("/{studentId}/progress")
    @PreAuthorize("hasAuthority('REPORT_PROGRESS_READ')")
    @Operation(summary = "Get student progress report", description = "Returns parent-ready progress, attendance, skill trends, recommendations, upcoming sessions, and WhatsApp copy.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student progress report retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<StudentProgressReportResponse>> getStudentProgress(
            @PathVariable UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        StudentProgressReportResponse response = studentProgressReportService.getStudentProgressReport(studentId, from, to);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Student progress report retrieved", response));
    }
}
