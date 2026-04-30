package com.example.boost.scheduling.api;

import com.example.boost.domain.dto.AttendanceBatchUpsertRequest;
import com.example.boost.domain.dto.AttendanceBatchUpsertResponse;
import com.example.boost.domain.dto.AttendanceSummaryResponse;
import com.example.boost.domain.dto.AttendanceSubmitRequest;
import com.example.boost.domain.dto.AttendanceUpsertRequest;
import com.example.boost.domain.dto.SessionAttendanceStudentResponse;
import com.example.boost.domain.dto.SessionAttendanceStudentUpsertRequest;
import com.example.boost.scheduling.application.AttendanceService;
import com.example.boost.common.api.ApiResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "10. Attendance")
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    @Operation(
            summary = "List attendance summaries",
            description = "Mengambil ringkasan absensi. Authority utama: ATTENDANCE_READ."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Attendance berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority ATTENDANCE_READ")
    })
    public ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> getSummaries() {
        List<AttendanceSummaryResponse> responses = attendanceService.getSummaries();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Attendance records retrieved", responses));
    }

    @GetMapping("/summary-count")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    @Operation(
            summary = "Get writable attendance summary count",
            description = "Mengambil jumlah ringkasan absensi yang bisa ditandai. Authority utama: ATTENDANCE_MARK."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jumlah attendance berhasil diambil"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority ATTENDANCE_MARK")
    })
    public ResponseEntity<ApiResponse<Long>> getWritableSummaryCount() {
        long count = attendanceService.getWritableSummaryCount();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Attendance writable summary count retrieved", count));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    @Operation(
            summary = "Submit attendance",
            description = "Mengirim event absensi untuk diproses asynchronous. Authority utama: ATTENDANCE_MARK."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Attendance berhasil di-submit"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - membutuhkan authority ATTENDANCE_MARK"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validasi payload gagal")
    })
    public ResponseEntity<ApiResponse<String>> submit(@Valid @RequestBody AttendanceSubmitRequest request) {
        attendanceService.submit(
                request.classGroupId(),
                request.sessionDate().toString(),
                request.presentCount(),
                request.absentCount()
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(HttpStatus.ACCEPTED.value(), "Attendance submitted event captured", "queued"));
    }

    @GetMapping("/sessions/{classSessionId}")
    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    public ResponseEntity<ApiResponse<List<SessionAttendanceStudentResponse>>> getSessionAttendances(
            @PathVariable UUID classSessionId) {
        List<SessionAttendanceStudentResponse> data = attendanceService.getSessionAttendances(classSessionId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Session attendance retrieved", data));
    }

    @PostMapping("/sessions/{classSessionId}/students")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public ResponseEntity<ApiResponse<String>> upsertStudentAttendanceBySession(
            @PathVariable UUID classSessionId,
            @Valid @RequestBody SessionAttendanceStudentUpsertRequest request) {
        attendanceService.upsertStudentAttendance(
                classSessionId,
                request.studentId(),
                new AttendanceUpsertRequest(request.attendanceStatus(), request.checkInAt(), request.remarks())
        );
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Attendance upserted", "ok"));
    }

    @PostMapping("/sessions/{classSessionId}/batch-upsert")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    @Operation(summary = "Batch upsert attendance by session")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Attendance batch upserted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<AttendanceBatchUpsertResponse>> upsertSessionAttendanceBatch(
            @PathVariable UUID classSessionId,
            @Valid @RequestBody AttendanceBatchUpsertRequest request) {
        AttendanceBatchUpsertResponse response = attendanceService.upsertSessionAttendanceBatch(classSessionId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Attendance batch upserted", response));
    }

    @PutMapping("/sessions/{classSessionId}/students/{studentId}")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public ResponseEntity<ApiResponse<String>> upsertStudentAttendance(
            @PathVariable UUID classSessionId,
            @PathVariable UUID studentId,
            @Valid @RequestBody AttendanceUpsertRequest request) {
        attendanceService.upsertStudentAttendance(classSessionId, studentId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Attendance upserted", "ok"));
    }
}
