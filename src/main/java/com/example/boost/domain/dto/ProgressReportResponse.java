package com.example.boost.domain.dto;

import java.util.List;
import java.util.UUID;

public record ProgressReportResponse(
        StudentSummary student,
        PeriodSummary period,
        AttendanceSummary attendanceSummary,
        List<SkillProgress> skillProgress,
        String coachNotes
) {
    public record StudentSummary(UUID id, String fullName, String studentCode) {}

    public record PeriodSummary(String from, String to) {}

    public record AttendanceSummary(long present, long absent, long late, long excused, long totalSessions) {}

    public record SkillProgress(String skillCode, String skillName, String averageScore, long assessmentCount) {}
}
