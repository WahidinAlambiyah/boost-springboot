package com.example.boost.dashboard.application;

import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.dashboard.infrastructure.DashboardRepository;
import com.example.boost.domain.dto.CoachDashboardResponse;
import com.example.boost.domain.dto.DashboardCommonResponses;
import com.example.boost.domain.dto.DashboardCommonResponses.AssessmentCompletionSummary;
import com.example.boost.domain.dto.DashboardCommonResponses.CoachSummaryCards;
import com.example.boost.domain.dto.DashboardCommonResponses.EventSummaryCards;
import com.example.boost.domain.dto.DashboardCommonResponses.LatestProgress;
import com.example.boost.domain.dto.DashboardCommonResponses.OwnerSummaryCards;
import com.example.boost.domain.dto.DashboardCommonResponses.PayrollSummary;
import com.example.boost.domain.dto.DashboardCommonResponses.Period;
import com.example.boost.domain.dto.EventDashboardResponse;
import com.example.boost.domain.dto.OwnerDashboardResponse;
import com.example.boost.domain.dto.ParentDashboardResponse;
import com.example.boost.security.CurrentActor;
import com.example.boost.security.CurrentActorProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        UUID resolvedAcademyId = resolveOwnerAcademyId(academyId);
        Period period = resolvePeriod(from, to);
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        var academy = dashboardRepository.findAcademy(resolvedAcademyId)
                .orElseThrow(() -> new NotFoundException("Academy not found"));
        long activeStudents = dashboardRepository.countActiveStudents(resolvedAcademyId);
        long assessedStudents = dashboardRepository.countAssessedActiveStudents(resolvedAcademyId, period.from(), period.to());
        long pendingAssessments = Math.max(0L, activeStudents - assessedStudents);
        var todaySessions = dashboardRepository.findOwnerTodaySessions(resolvedAcademyId, today);

        OwnerSummaryCards summaryCards = new OwnerSummaryCards(
                activeStudents,
                dashboardRepository.countActiveCoaches(resolvedAcademyId),
                todaySessions.size(),
                dashboardRepository.attendanceRate(resolvedAcademyId, period.from(), period.to()),
                pendingAssessments,
                0L,
                dashboardRepository.countUnpaidInvoices(resolvedAcademyId),
                dashboardRepository.currentMonthPayrollStatus(resolvedAcademyId, currentMonth.getMonthValue(), currentMonth.getYear())
        );

        long notAssessedStudents = Math.max(0L, activeStudents - assessedStudents);
        AssessmentCompletionSummary assessmentCompletion = new AssessmentCompletionSummary(
                activeStudents,
                assessedStudents,
                notAssessedStudents,
                percentage(assessedStudents, activeStudents)
        );

        PayrollSummary payrollSummary = dashboardRepository.payrollSummary(resolvedAcademyId, currentMonth);

        return new OwnerDashboardResponse(
                academy,
                period,
                summaryCards,
                todaySessions,
                dashboardRepository.findStudentsWithoutAssessment(resolvedAcademyId, period.from(), period.to()),
                List.of(),
                dashboardRepository.findRecentAssessments(resolvedAcademyId, period.from(), period.to()),
                dashboardRepository.attendanceTrend(resolvedAcademyId, period.from(), period.to()),
                assessmentCompletion,
                dashboardRepository.levelDistribution(resolvedAcademyId),
                dashboardRepository.packageSummary(resolvedAcademyId, today),
                dashboardRepository.financeSummary(resolvedAcademyId, period.from(), period.to(), today),
                payrollSummary
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

    private UUID resolveOwnerAcademyId(UUID requestedAcademyId) {
        if (requestedAcademyId != null) {
            return requestedAcademyId;
        }

        CurrentActor actor = currentActorProvider.getCurrentActor();
        if (isSuperAdmin(actor)) {
            return dashboardRepository.findFirstActiveAcademyId()
                    .orElseThrow(() -> new NotFoundException("Academy not found"));
        }

        List<UUID> academyIds = dashboardRepository.findAccessibleAcademyIds(actor.userId(), 2);
        if (academyIds.isEmpty()) {
            throw new NotFoundException("Academy not found");
        }
        if (academyIds.size() > 1) {
            throw new BadRequestException("academyId is required when multiple academies are accessible");
        }
        return academyIds.get(0);
    }

    private boolean isSuperAdmin(CurrentActor actor) {
        return actor.hasRole("ADMIN") || actor.hasRole("SUPER_ADMIN");
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator <= 0L) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, java.math.RoundingMode.HALF_UP);
    }
}
