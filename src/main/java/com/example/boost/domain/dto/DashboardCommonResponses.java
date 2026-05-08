package com.example.boost.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class DashboardCommonResponses {
    private DashboardCommonResponses() {
    }

    public record AcademySummary(UUID id, String name, String code) {
    }

    public record Period(LocalDate from, LocalDate to) {
    }

    public record OwnerSummaryCards(
            long activeStudents,
            long activeCoaches,
            long todaySessions,
            BigDecimal attendanceRate,
            long pendingAssessments,
            long upcomingEvents,
            long unpaidInvoices,
            String currentMonthPayrollStatus
    ) {
    }

    public record OwnerTodaySession(
            UUID id,
            String title,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            String locationName,
            String classGroupName,
            String coachNames,
            String status,
            String attendanceStatus
    ) {
    }

    public record StudentNeedAttention(
            UUID studentId,
            String studentName,
            String nickname,
            String currentLevel,
            String reason,
            LocalDate lastAttendanceDate,
            LocalDate lastAssessmentDate
    ) {
    }

    public record DashboardEventItem(
            UUID id,
            String title,
            LocalDate eventDate,
            String locationName,
            String ageCategory,
            Integer quota,
            Long registeredCount,
            String status
    ) {
    }

    public record RecentAssessment(
            UUID assessmentId,
            UUID studentId,
            String studentName,
            UUID classSessionId,
            LocalDate sessionDate,
            String coachName,
            String overallNotes,
            String recommendation
    ) {
    }

    public record CoachSummary(UUID id, UUID userId, String name, UUID academyId) {
    }

    public record CoachSummaryCards(
            long todaySessions,
            long pendingAttendance,
            long pendingAssessments,
            long completedAssessmentsThisMonth
    ) {
    }

    public record CoachTodaySession(
            UUID id,
            String title,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            String locationName,
            String classGroupName,
            long studentCount,
            boolean attendanceSubmitted,
            long assessmentCompletedCount,
            String status
    ) {
    }

    public record PendingAssessmentStudent(
            UUID studentId,
            String studentName,
            String nickname,
            UUID classSessionId,
            LocalDate sessionDate,
            String currentLevel
    ) {
    }

    public record ParentStudentSummary(
            UUID id,
            String studentNo,
            String fullName,
            String nickname,
            String currentLevel,
            String academyName
    ) {
    }

    public record ParentAttendanceSummary(
            long totalSessions,
            long present,
            long absent,
            long permit,
            long sick,
            long late,
            BigDecimal attendanceRate
    ) {
    }

    public record LatestProgress(
            LocalDate latestAssessmentDate,
            String coachName,
            String overallNotes,
            String recommendation
    ) {
    }

    public record SkillProgress(
            String skillCode,
            String skillName,
            BigDecimal latestScore,
            BigDecimal averageScore,
            BigDecimal previousScore,
            String trend
    ) {
    }

    public record UpcomingSession(
            UUID sessionId,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            String locationName,
            String classGroupName,
            String status
    ) {
    }

    public record EventSummaryCards(
            long totalEvents,
            long upcomingEvents,
            long completedEvents,
            long totalParticipants
    ) {
    }
}
