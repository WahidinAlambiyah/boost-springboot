package com.example.boost.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StudentProgressReportResponse(
        StudentIdentity student,
        Period period,
        AttendanceSummary attendance,
        SkillProgress skillProgress,
        List<CoachNoteBySessionDate> coachNotes
) {
    public record StudentIdentity(UUID id, String studentNo, String fullName, String nickname, String currentLevel) {}

    public record Period(LocalDate from, LocalDate to) {}

    public record AttendanceSummary(int totalSessions, int present, int permit, int absent) {}

    public record SkillProgress(BigDecimal latestScore, BigDecimal averageScore, BigDecimal previousScore, Trend trend) {}

    public enum Trend {
        UP,
        DOWN,
        FLAT
    }

    public record CoachNoteBySessionDate(LocalDate sessionDate, String note) {}
}
