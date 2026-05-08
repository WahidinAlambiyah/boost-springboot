package com.example.boost.reporting.application;

import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.StudentProgressReportResponse;
import com.example.boost.reporting.infrastructure.StudentProgressReportRepository;
import com.example.boost.security.CurrentActor;
import com.example.boost.security.CurrentActorProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentProgressReportService {
    private final StudentProgressReportRepository repository;
    private final CurrentActorProvider currentActorProvider;

    public StudentProgressReportResponse getStudentProgressReport(UUID studentId, LocalDate from, LocalDate to) {
        StudentProgressReportResponse.Period reportPeriod = resolvePeriod(from, to);
        StudentProgressReportResponse.StudentIdentity student = repository.findStudentIdentity(studentId);
        if (student == null) {
            throw new NotFoundException("student not found");
        }

        CurrentActor actor = currentActorProvider.getCurrentActor();
        if (!repository.canAccessStudentProgress(actor, studentId)) {
            throw new AccessDeniedException("Forbidden");
        }

        StudentProgressReportResponse.AttendanceSummary attendance = withAttendanceRate(
                repository.findAttendanceSummary(studentId, reportPeriod.from(), reportPeriod.to())
        );
        List<StudentProgressReportResponse.SkillProgress> skillProgress = repository.findSkillProgress(studentId, reportPeriod.from(), reportPeriod.to())
                .stream()
                .map(this::withTrend)
                .toList();
        List<StudentProgressReportResponse.CoachNote> coachNotes = repository.findCoachNotes(studentId, reportPeriod.from(), reportPeriod.to());
        List<StudentProgressReportResponse.NextRecommendation> nextRecommendations = buildRecommendations(skillProgress, coachNotes);

        return new StudentProgressReportResponse(
                withAgeText(student),
                reportPeriod,
                attendance,
                repository.findAttendanceTimeline(studentId, reportPeriod.from(), reportPeriod.to()),
                skillProgress,
                coachNotes,
                nextRecommendations,
                repository.findUpcomingSessions(studentId, LocalDate.now()),
                buildWhatsappSummary(student, reportPeriod, attendance, skillProgress, coachNotes, nextRecommendations)
        );
    }

    StudentProgressReportResponse.Trend calculateTrend(BigDecimal latestScore, BigDecimal previousScore) {
        if (latestScore == null || previousScore == null) {
            return StudentProgressReportResponse.Trend.FLAT;
        }
        int compare = latestScore.compareTo(previousScore);
        if (compare > 0) {
            return StudentProgressReportResponse.Trend.UP;
        }
        if (compare < 0) {
            return StudentProgressReportResponse.Trend.DOWN;
        }
        return StudentProgressReportResponse.Trend.FLAT;
    }

    private StudentProgressReportResponse.Period resolvePeriod(LocalDate from, LocalDate to) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate resolvedFrom = from == null ? currentMonth.atDay(1) : from;
        LocalDate resolvedTo = to == null ? currentMonth.atEndOfMonth() : to;
        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new BadRequestException("from must be before or equal to to");
        }
        return new StudentProgressReportResponse.Period(resolvedFrom, resolvedTo);
    }

    private StudentProgressReportResponse.StudentIdentity withAgeText(StudentProgressReportResponse.StudentIdentity student) {
        return new StudentProgressReportResponse.StudentIdentity(
                student.id(),
                student.studentNo(),
                student.fullName(),
                student.nickname(),
                student.dateOfBirth(),
                ageText(student.dateOfBirth()),
                student.currentLevel(),
                student.academyName(),
                student.guardianNames() == null ? List.of() : student.guardianNames()
        );
    }

    private String ageText(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        Period age = Period.between(dateOfBirth, LocalDate.now());
        if (age.getYears() <= 0) {
            return age.getMonths() + " bulan";
        }
        if (age.getMonths() <= 0) {
            return age.getYears() + " tahun";
        }
        return age.getYears() + " tahun " + age.getMonths() + " bulan";
    }

    private StudentProgressReportResponse.AttendanceSummary withAttendanceRate(StudentProgressReportResponse.AttendanceSummary attendance) {
        if (attendance == null) {
            return new StudentProgressReportResponse.AttendanceSummary(0, 0, 0, 0, 0, 0, BigDecimal.ZERO.setScale(2));
        }
        BigDecimal rate = attendance.totalSessions() <= 0
                ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(attendance.present())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(attendance.totalSessions()), 2, RoundingMode.HALF_UP);
        return new StudentProgressReportResponse.AttendanceSummary(
                attendance.totalSessions(),
                attendance.present(),
                attendance.absent(),
                attendance.permit(),
                attendance.sick(),
                attendance.late(),
                rate
        );
    }

    private StudentProgressReportResponse.SkillProgress withTrend(StudentProgressReportResponse.SkillProgress skill) {
        return new StudentProgressReportResponse.SkillProgress(
                skill.skillCode(),
                skill.skillName(),
                skill.latestScore(),
                skill.averageScore() == null ? null : skill.averageScore().setScale(2, RoundingMode.HALF_UP),
                skill.previousScore(),
                calculateTrend(skill.latestScore(), skill.previousScore()),
                skill.maxScore(),
                skill.latestNotes()
        );
    }

    private List<StudentProgressReportResponse.NextRecommendation> buildRecommendations(
            List<StudentProgressReportResponse.SkillProgress> skillProgress,
            List<StudentProgressReportResponse.CoachNote> coachNotes
    ) {
        List<StudentProgressReportResponse.NextRecommendation> recommendations = new ArrayList<>();
        if (skillProgress == null || skillProgress.isEmpty()) {
            recommendations.add(new StudentProgressReportResponse.NextRecommendation(
                    "Belum ada penilaian",
                    "Belum ada penilaian pada periode ini. Tetap dampingi anak untuk hadir konsisten di sesi latihan berikutnya.",
                    "MEDIUM"
            ));
            return recommendations;
        }

        skillProgress.stream()
                .filter(skill -> skill.latestScore() != null)
                .min(Comparator.comparing(StudentProgressReportResponse.SkillProgress::latestScore))
                .ifPresent(skill -> recommendations.add(new StudentProgressReportResponse.NextRecommendation(
                        "Latihan " + skill.skillName(),
                        "Fokuskan latihan berikutnya pada " + skill.skillName() + " agar progres semakin stabil.",
                        "HIGH"
                )));

        if (coachNotes != null && !coachNotes.isEmpty()) {
            recommendations.add(new StudentProgressReportResponse.NextRecommendation(
                    "Ikuti catatan pelatih",
                    "Baca catatan pelatih terbaru dan jadikan panduan latihan ringan di rumah.",
                    "MEDIUM"
            ));
        }
        return recommendations;
    }

    private String buildWhatsappSummary(
            StudentProgressReportResponse.StudentIdentity student,
            StudentProgressReportResponse.Period period,
            StudentProgressReportResponse.AttendanceSummary attendance,
            List<StudentProgressReportResponse.SkillProgress> skillProgress,
            List<StudentProgressReportResponse.CoachNote> coachNotes,
            List<StudentProgressReportResponse.NextRecommendation> recommendations
    ) {
        String displayName = student.nickname() == null || student.nickname().isBlank() ? student.fullName() : student.nickname();
        StringBuilder summary = new StringBuilder();
        summary.append("Halo Bapak/Ibu, berikut ringkasan progres ").append(displayName)
                .append(" periode ").append(period.from()).append(" s.d. ").append(period.to()).append(". ");
        summary.append("Kehadiran: ").append(attendance.present()).append(" dari ").append(attendance.totalSessions())
                .append(" sesi (").append(attendance.attendanceRate()).append("%). ");

        if (skillProgress == null || skillProgress.isEmpty()) {
            summary.append("Belum ada penilaian pada periode ini. ");
        } else {
            StudentProgressReportResponse.SkillProgress latest = skillProgress.get(0);
            summary.append("Penilaian terbaru: ").append(latest.skillName()).append(" skor ").append(latest.latestScore())
                    .append(" dari ").append(latest.maxScore() == null ? "-" : latest.maxScore())
                    .append(", tren ").append(latest.trend()).append(". ");
        }

        if (coachNotes != null && !coachNotes.isEmpty() && coachNotes.get(0).overallNotes() != null) {
            summary.append("Catatan pelatih: ").append(coachNotes.get(0).overallNotes()).append(" ");
        }
        if (recommendations != null && !recommendations.isEmpty()) {
            summary.append("Rekomendasi: ").append(recommendations.get(0).description());
        }
        return summary.toString().trim();
    }
}
