package com.example.boost.attendance.repository;

import com.example.boost.attendance.dto.AttendanceSummaryResponse;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AttendanceRepository {
    public List<AttendanceSummaryResponse> findSummaries() {
        return List.of(
                AttendanceSummaryResponse.builder()
                        .code("ATTENDANCE-001")
                        .name("Default Attendance Record")
                        .status("ACTIVE")
                        .build()
        );
    }
}
