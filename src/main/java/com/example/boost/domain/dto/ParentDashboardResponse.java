package com.example.boost.domain.dto;

import java.util.List;

public record ParentDashboardResponse(
        DashboardCommonResponses.ParentStudentSummary student,
        DashboardCommonResponses.Period period,
        DashboardCommonResponses.ParentAttendanceSummary attendanceSummary,
        DashboardCommonResponses.LatestProgress latestProgress,
        List<DashboardCommonResponses.SkillProgress> skillProgress,
        List<DashboardCommonResponses.UpcomingSession> upcomingSessions,
        List<DashboardCommonResponses.DashboardEventItem> upcomingEvents
) {
}
