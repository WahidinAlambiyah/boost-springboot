package com.example.boost.domain.dto;

import java.util.List;

public record EventDashboardResponse(
        DashboardCommonResponses.Period period,
        DashboardCommonResponses.EventSummaryCards summaryCards,
        List<DashboardCommonResponses.DashboardEventItem> upcomingEvents,
        List<DashboardCommonResponses.DashboardEventItem> recentEvents
) {
}
