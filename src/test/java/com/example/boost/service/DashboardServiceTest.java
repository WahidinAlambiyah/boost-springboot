package com.example.boost.service;

import com.example.boost.dashboard.application.DashboardService;
import com.example.boost.dashboard.infrastructure.DashboardRepository;
import com.example.boost.domain.dto.DashboardCommonResponses.AcademySummary;
import com.example.boost.domain.dto.DashboardCommonResponses.OwnerTodaySession;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardServiceTest {

    @Test
    void ownerShouldBuildSummaryCardsAndPendingAssessmentCount() {
        UUID academyId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 31);
        DashboardService dashboardService = new DashboardService(new OwnerDashboardRepositoryStub(academyId, from, to), null);

        var response = dashboardService.owner(academyId, from, to);

        assertThat(response.academy().id()).isEqualTo(academyId);
        assertThat(response.period().from()).isEqualTo(from);
        assertThat(response.period().to()).isEqualTo(to);
        assertThat(response.summaryCards().activeStudents()).isEqualTo(10L);
        assertThat(response.summaryCards().activeCoaches()).isEqualTo(3L);
        assertThat(response.summaryCards().pendingAssessments()).isEqualTo(6L);
        assertThat(response.summaryCards().attendanceRate()).isEqualByComparingTo("75.00");
        assertThat(response.summaryCards().unpaidInvoices()).isEqualTo(2L);
        assertThat(response.summaryCards().currentMonthPayrollStatus()).isEqualTo("CALCULATED");
        assertThat(response.upcomingEvents()).isEmpty();
    }

    private static class OwnerDashboardRepositoryStub extends DashboardRepository {
        private final UUID academyId;
        private final LocalDate from;
        private final LocalDate to;

        OwnerDashboardRepositoryStub(UUID academyId, LocalDate from, LocalDate to) {
            super(null);
            this.academyId = academyId;
            this.from = from;
            this.to = to;
        }

        @Override
        public Optional<AcademySummary> findAcademy(UUID academyId) {
            assertThat(academyId).isEqualTo(this.academyId);
            return Optional.of(new AcademySummary(academyId, "Pushbike Academy", "PBA"));
        }

        @Override
        public long countActiveStudents(UUID academyId) {
            return 10L;
        }

        @Override
        public long countAssessedActiveStudents(UUID academyId, LocalDate from, LocalDate to) {
            assertThat(from).isEqualTo(this.from);
            assertThat(to).isEqualTo(this.to);
            return 4L;
        }

        @Override
        public long countActiveCoaches(UUID academyId) {
            return 3L;
        }

        @Override
        public List<OwnerTodaySession> findOwnerTodaySessions(UUID academyId, LocalDate today) {
            return List.of();
        }

        @Override
        public BigDecimal attendanceRate(UUID academyId, LocalDate from, LocalDate to) {
            return new BigDecimal("75.00");
        }

        @Override
        public long countUnpaidInvoices(UUID academyId) {
            return 2L;
        }

        @Override
        public String currentMonthPayrollStatus(UUID academyId, int month, int year) {
            return "CALCULATED";
        }

        @Override
        public List<com.example.boost.domain.dto.DashboardCommonResponses.StudentNeedAttention> findStudentsWithoutAssessment(UUID academyId, LocalDate from, LocalDate to) {
            return List.of();
        }

        @Override
        public List<com.example.boost.domain.dto.DashboardCommonResponses.RecentAssessment> findRecentAssessments(UUID academyId, LocalDate from, LocalDate to) {
            return List.of();
        }
    }
}
