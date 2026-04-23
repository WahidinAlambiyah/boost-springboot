package com.example.boost.controller;

import com.example.boost.domain.dto.AttendanceSummaryResponse;
import com.example.boost.service.AttendanceService;
import com.example.boost.domain.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    public ResponseEntity<ApiResponse<List<AttendanceSummaryResponse>>> getSummaries() {
        List<AttendanceSummaryResponse> responses = attendanceService.getSummaries();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Attendance records retrieved", responses));
    }

    @GetMapping("/summary-count")
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public ResponseEntity<ApiResponse<Long>> getWritableSummaryCount() {
        long count = attendanceService.getWritableSummaryCount();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Attendance writable summary count retrieved", count));
    }
}
