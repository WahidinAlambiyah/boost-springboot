package com.example.boost.domain.dto;

import java.util.List;

public record OwnerDashboardResponse(
        DashboardCommonResponses.AcademySummary academy,
        DashboardCommonResponses.Period period,
        DashboardCommonResponses.OwnerSummaryCards summaryCards,
        List<DashboardCommonResponses.OwnerTodaySession> todaySessions,
        List<DashboardCommonResponses.StudentNeedAttention> studentsNeedAttention,
        List<DashboardCommonResponses.DashboardEventItem> upcomingEvents,
        List<DashboardCommonResponses.RecentAssessment> recentAssessments,
        List<DashboardCommonResponses.AttendanceTrendItem> attendanceTrend,
        DashboardCommonResponses.AssessmentCompletionSummary assessmentCompletion,
        List<DashboardCommonResponses.LevelDistributionItem> levelDistribution,
        DashboardCommonResponses.PackageSummary packageSummary,
        DashboardCommonResponses.FinanceSummary financeSummary,
        DashboardCommonResponses.PayrollSummary payrollSummary
) {
}
