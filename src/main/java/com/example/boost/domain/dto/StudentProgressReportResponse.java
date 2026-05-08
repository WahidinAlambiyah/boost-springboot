package com.example.boost.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record StudentProgressReportResponse(
        StudentIdentity student,
        Period period,
        AttendanceSummary attendanceSummary,
        List<AttendanceTimelineItem> attendanceTimeline,
        List<SkillProgress> skillProgress,
        List<CoachNote> coachNotes,
        List<NextRecommendation> nextRecommendations,
        List<UpcomingSession> upcomingSessions,
        String whatsappSummary
) {
    public record StudentIdentity(
            UUID id,
            String studentNo,
            String fullName,
            String nickname,
            LocalDate dateOfBirth,
            String ageText,
            String currentLevel,
            String academyName,
            List<String> guardianNames
    ) {
    }

    public record Period(LocalDate from, LocalDate to) {
    }

    public record AttendanceSummary(
            long totalSessions,
            long present,
            long absent,
            long permit,
            long sick,
            long late,
            BigDecimal attendanceRate
    ) {
    }

    public record AttendanceTimelineItem(
            UUID sessionId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            String classGroupName,
            String locationName,
            String status,
            String remarks
    ) {
    }

    public record SkillProgress(
            String skillCode,
            String skillName,
            BigDecimal latestScore,
            BigDecimal averageScore,
            BigDecimal previousScore,
            Trend trend,
            Integer maxScore,
            String latestNotes
    ) {
    }

    public enum Trend {
        UP,
        DOWN,
        FLAT
    }

    public record CoachNote(
            UUID assessmentId,
            LocalDate sessionDate,
            String coachName,
            String overallNotes,
            String recommendation
    ) {
    }

    public record NextRecommendation(
            String title,
            String description,
            String priority
    ) {
    }

    public record UpcomingSession(
            UUID sessionId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            String classGroupName,
            String locationName
    ) {
    }
}
