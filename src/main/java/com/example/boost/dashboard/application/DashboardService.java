package com.example.boost.dashboard.application;

import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.dashboard.infrastructure.DashboardRepository;
import com.example.boost.domain.dto.CoachDashboardResponse;
import com.example.boost.domain.dto.DashboardCommonResponses;
import com.example.boost.domain.dto.DashboardCommonResponses.CoachSummaryCards;
import com.example.boost.domain.dto.DashboardCommonResponses.EventSummaryCards;
import com.example.boost.domain.dto.DashboardCommonResponses.LatestProgress;
import com.example.boost.domain.dto.DashboardCommonResponses.OwnerSummaryCards;
import com.example.boost.domain.dto.DashboardCommonResponses.Period;
import com.example.boost.domain.dto.EventDashboardResponse;
import com.example.boost.domain.dto.OwnerDashboardResponse;
import com.example.boost.domain.dto.ParentDashboardResponse;
import com.example.boost.security.CurrentActorProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final CurrentActorProvider currentActorProvider;

    @Transactional(readOnly = true)
    public OwnerDashboardResponse owner(UUID academyId, LocalDate from, LocalDate to) {
        Period period = resolvePeriod(from, to);
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        var academy = dashboardRepository.findAcademy(academyId)
                .orElseThrow(() -> new NotFoundException("Academy not found"));
        long activeStudents = dashboardRepository.countActiveStudents(academyId);
        long pendingAssessments = Math.max(0L, activeStudents - dashboardRepository.countAssessedActiveStudents(academyId, period.from(), period.to()));
        var todaySessions = dashboardRepository.findOwnerTodaySessions(academyId, today);

        OwnerSummaryCards summaryCards = new OwnerSummaryCards(
                activeStudents,
                dashboardRepository.countActiveCoaches(academyId),
                todaySessions.size(),
                dashboardRepository.attendanceRate(academyId, period.from(), period.to()),
                pendingAssessments,
                0L,
                dashboardRepository.countUnpaidInvoices(academyId),
                dashboardRepository.currentMonthPayrollStatus(academyId, currentMonth.getMonthValue(), currentMonth.getYear())
        );

        return new OwnerDashboardResponse(
                academy,
                period,
                summaryCards,
                todaySessions,
                dashboardRepository.findStudentsWithoutAssessment(academyId, period.from(), period.to()),
                List.of(),
                dashboardRepository.findRecentAssessments(academyId, period.from(), period.to())
        );
    }

    @Transactional(readOnly = true)
    public CoachDashboardResponse coach(UUID academyId, LocalDate from, LocalDate to) {
        Period period = resolvePeriod(from, to);
        LocalDate today = LocalDate.now();
        UUID userId = currentActorProvider.currentUserId();
        var coach = dashboardRepository.findCoachByUserId(userId, academyId)
                .orElseThrow(() -> new NotFoundException("Coach profile not found"));
        var todaySessions = dashboardRepository.findCoachTodaySessions(academyId, coach.id(), today);
        var pendingAssessmentStudents = dashboardRepository.findCoachPendingAssessmentStudents(academyId, coach.id(), period.from(), period.to());
        YearMonth currentMonth = YearMonth.now();
        CoachSummaryCards summaryCards = new CoachSummaryCards(
                todaySessions.size(),
                dashboardRepository.countCoachPendingAttendance(academyId, coach.id(), period.from(), period.to()),
                pendingAssessmentStudents.size(),
                dashboardRepository.countCoachCompletedAssessments(academyId, coach.id(), currentMonth.atDay(1), currentMonth.atEndOfMonth())
        );
        return new CoachDashboardResponse(coach, period, summaryCards, todaySessions, pendingAssessmentStudents);
    }

    @Transactional(readOnly = true)
    public ParentDashboardResponse parent(UUID studentId, LocalDate from, LocalDate to) {
        Period period = resolvePeriod(from, to);
        var student = dashboardRepository.findParentStudent(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        LatestProgress latestProgress = dashboardRepository.latestProgress(studentId)
                .orElse(new LatestProgress(null, null, null, null));
        return new ParentDashboardResponse(
                student,
                period,
                dashboardRepository.parentAttendanceSummary(studentId, period.from(), period.to()),
                latestProgress,
                dashboardRepository.skillProgress(studentId),
                dashboardRepository.upcomingSessions(studentId, LocalDate.now()),
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public EventDashboardResponse event(UUID academyId, LocalDate from, LocalDate to) {
        Period period = resolvePeriod(from, to);
        // Event module is not available yet; keep API contract ready with zero-summary and empty collections.
        return new EventDashboardResponse(
                period,
                new EventSummaryCards(0L, 0L, 0L, 0L),
                List.of(),
                List.of()
        );
    }

    private Period resolvePeriod(LocalDate from, LocalDate to) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate resolvedFrom = from == null ? currentMonth.atDay(1) : from;
        LocalDate resolvedTo = to == null ? currentMonth.atEndOfMonth() : to;
        if (resolvedTo.isBefore(resolvedFrom)) {
            throw new BadRequestException("to must be on or after from");
        }
        return new DashboardCommonResponses.Period(resolvedFrom, resolvedTo);
    }
}
