package com.example.boost.repository;

import com.example.boost.domain.dto.SchedulingSummaryResponse;

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
