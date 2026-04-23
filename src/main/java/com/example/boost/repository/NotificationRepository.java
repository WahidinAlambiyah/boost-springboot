package com.example.boost.repository;

import com.example.boost.domain.dto.NotificationSummaryResponse;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationRepository {
    public List<NotificationSummaryResponse> findSummaries() {
        return List.of(
                NotificationSummaryResponse.builder()
                        .code("NOTIFICATION-001")
                        .name("Default Notification Record")
                        .status("ACTIVE")
                        .build()
        );
    }
}
