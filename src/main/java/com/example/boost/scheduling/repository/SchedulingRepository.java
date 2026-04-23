package com.example.boost.scheduling.repository;

import com.example.boost.scheduling.dto.SchedulingSummaryResponse;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SchedulingRepository {
    public List<SchedulingSummaryResponse> findSummaries() {
        return List.of(
                SchedulingSummaryResponse.builder()
                        .code("SCHEDULING-001")
                        .name("Default Scheduling Record")
                        .status("ACTIVE")
                        .build()
        );
    }
}
