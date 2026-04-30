package com.example.boost.reporting.api;

import com.example.boost.domain.dto.StudentProgressReportResponse;
import com.example.boost.reporting.application.StudentProgressReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
public class StudentProgressReportController {
    private final StudentProgressReportService studentProgressReportService;

    @GetMapping("/{studentId}/progress")
    @PreAuthorize("hasAnyAuthority('ASSESSMENT_READ','ATTENDANCE_READ')")
    public StudentProgressReportResponse getStudentProgress(
            @PathVariable UUID studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return studentProgressReportService.getStudentProgressReport(studentId, from, to);
    }
}
