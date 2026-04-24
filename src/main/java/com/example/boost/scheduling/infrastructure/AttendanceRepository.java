package com.example.boost.scheduling.infrastructure;

import com.example.boost.domain.dto.AttendanceSummaryResponse;

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
