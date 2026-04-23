package com.example.boost.enrollment.repository;

import com.example.boost.enrollment.dto.EnrollmentSummaryResponse;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EnrollmentRepository {
    public List<EnrollmentSummaryResponse> findSummaries() {
        return List.of(
                EnrollmentSummaryResponse.builder()
                        .code("ENROLLMENT-001")
                        .name("Default Enrollment Record")
                        .status("ACTIVE")
                        .build()
        );
    }
}
