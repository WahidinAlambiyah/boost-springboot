package com.example.boost.service;

import com.example.boost.domain.dto.SchedulingSummaryResponse;
import com.example.boost.repository.SchedulingRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulingService {
    private final SchedulingRepository schedulingRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    public List<SchedulingSummaryResponse> getSummaries() {
        return schedulingRepository.findSummaries();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCHEDULE_WRITE')")
    public long getWritableSummaryCount() {
        return schedulingRepository.findSummaries().size();
    }
}
