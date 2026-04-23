package com.example.boost.service;

import com.example.boost.domain.dto.NotificationSummaryResponse;
import com.example.boost.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public List<NotificationSummaryResponse> getSummaries() {
        return notificationRepository.findSummaries();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('NOTIFICATION_WRITE')")
    public long getWritableSummaryCount() {
        return notificationRepository.findSummaries().size();
    }
}
