package com.example.boost.service;

import com.example.boost.domain.dto.AttendanceSummaryResponse;
import com.example.boost.repository.AttendanceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ATTENDANCE_READ')")
    public List<AttendanceSummaryResponse> getSummaries() {
        return attendanceRepository.findSummaries();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public long getWritableSummaryCount() {
        return attendanceRepository.findSummaries().size();
    }
}
