package com.example.boost.domain.dto;

import java.util.List;

public record CoachDashboardResponse(
        DashboardCommonResponses.CoachSummary coach,
        DashboardCommonResponses.Period period,
        DashboardCommonResponses.CoachSummaryCards summaryCards,
        List<DashboardCommonResponses.CoachTodaySession> todaySessions,
        List<DashboardCommonResponses.PendingAssessmentStudent> pendingAssessmentStudents
) {
}
