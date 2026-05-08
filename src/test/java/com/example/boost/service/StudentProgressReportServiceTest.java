package com.example.boost.service;

import com.example.boost.domain.dto.StudentProgressReportResponse;
import com.example.boost.reporting.application.StudentProgressReportService;
import com.example.boost.reporting.infrastructure.StudentProgressReportRepository;
import com.example.boost.security.CurrentActor;
import com.example.boost.security.CurrentActorProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StudentProgressReportServiceTest {

    @Test
    void shouldCalculateSkillTrendUp() {
        UUID studentId = UUID.randomUUID();
        StudentProgressReportService service = new StudentProgressReportService(
                new ReportRepositoryStub(studentId, List.of(new StudentProgressReportResponse.SkillProgress(
                        "BALANCE", "Balance", new BigDecimal("4.50"), new BigDecimal("4.00"),
                        new BigDecimal("3.50"), null, 5, "Makin stabil"
                ))),
                actorProvider()
        );

        StudentProgressReportResponse response = service.getStudentProgressReport(studentId,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

        assertThat(response.skillProgress()).hasSize(1);
        assertThat(response.skillProgress().get(0).trend()).isEqualTo(StudentProgressReportResponse.Trend.UP);
        assertThat(response.whatsappSummary()).contains("tren UP");
    }

    @Test
    void shouldReturnNoAssessmentMessageWhenAssessmentMissing() {
        UUID studentId = UUID.randomUUID();
        StudentProgressReportService service = new StudentProgressReportService(
                new ReportRepositoryStub(studentId, List.of()),
                actorProvider()
        );

        StudentProgressReportResponse response = service.getStudentProgressReport(studentId,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

        assertThat(response.skillProgress()).isEmpty();
        assertThat(response.nextRecommendations()).hasSize(1);
        assertThat(response.nextRecommendations().get(0).description()).contains("Belum ada penilaian pada periode ini");
        assertThat(response.whatsappSummary()).contains("Belum ada penilaian pada periode ini");
        assertThat(response.attendanceSummary().attendanceRate()).isEqualByComparingTo("75.00");
    }

    private CurrentActorProvider actorProvider() {
        return new CurrentActorProvider() {
            @Override
            public CurrentActor getCurrentActor() {
                return new CurrentActor(UUID.randomUUID(), "admin@example.com", Set.of("ADMIN"));
            }
        };
    }

    private static class ReportRepositoryStub extends StudentProgressReportRepository {
        private final UUID studentId;
        private final List<StudentProgressReportResponse.SkillProgress> skillProgress;

        ReportRepositoryStub(UUID studentId, List<StudentProgressReportResponse.SkillProgress> skillProgress) {
            super(null);
            this.studentId = studentId;
            this.skillProgress = skillProgress;
        }

        @Override
        public StudentProgressReportResponse.StudentIdentity findStudentIdentity(UUID studentId) {
            assertThat(studentId).isEqualTo(this.studentId);
            return new StudentProgressReportResponse.StudentIdentity(
                    studentId,
                    "STD-001",
                    "Rafa Pratama",
                    "Rafa",
                    LocalDate.of(2020, 1, 10),
                    null,
                    "BEGINNER",
                    "Pushbike Academy",
                    List.of("Bapak A")
            );
        }

        @Override
        public boolean canAccessStudentProgress(CurrentActor actor, UUID studentId) {
            return true;
        }

        @Override
        public StudentProgressReportResponse.AttendanceSummary findAttendanceSummary(UUID studentId, LocalDate from, LocalDate to) {
            return new StudentProgressReportResponse.AttendanceSummary(4, 3, 1, 0, 0, 0, null);
        }

        @Override
        public List<StudentProgressReportResponse.AttendanceTimelineItem> findAttendanceTimeline(UUID studentId, LocalDate from, LocalDate to) {
            return List.of();
        }

        @Override
        public List<StudentProgressReportResponse.SkillProgress> findSkillProgress(UUID studentId, LocalDate from, LocalDate to) {
            return skillProgress;
        }

        @Override
        public List<StudentProgressReportResponse.CoachNote> findCoachNotes(UUID studentId, LocalDate from, LocalDate to) {
            return List.of();
        }

        @Override
        public List<StudentProgressReportResponse.UpcomingSession> findUpcomingSessions(UUID studentId, LocalDate from) {
            return List.of();
        }
    }
}
