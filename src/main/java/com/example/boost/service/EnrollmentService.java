package com.example.boost.service;

import com.example.boost.domain.dto.EnrollmentSummaryResponse;
import com.example.boost.repository.EnrollmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ENROLLMENT_READ')")
    public List<EnrollmentSummaryResponse> getSummaries() {
        return enrollmentRepository.findSummaries();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ENROLLMENT_WRITE')")
    public long getWritableSummaryCount() {
        return enrollmentRepository.findSummaries().size();
    }
}
