package com.example.boost.scheduling.application;

import com.example.boost.domain.dto.AttendanceSummaryResponse;
import com.example.boost.notification.application.OutboxEventService;
import com.example.boost.scheduling.infrastructure.AttendanceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final OutboxEventService outboxEventService;

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

    @Transactional
    @PreAuthorize("hasAuthority('ATTENDANCE_MARK')")
    public void submit(UUID classGroupId, String sessionDate, int presentCount, int absentCount) {
        outboxEventService.append(
                "ATTENDANCE",
                classGroupId,
                "attendance.submitted",
                Map.of(
                        "classGroupId", classGroupId,
                        "sessionDate", sessionDate,
                        "presentCount", presentCount,
                        "absentCount", absentCount
                )
        );
    }
}
