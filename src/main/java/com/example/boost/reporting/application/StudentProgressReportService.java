package com.example.boost.reporting.application;

import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.StudentProgressReportResponse;
import com.example.boost.reporting.infrastructure.StudentProgressReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentProgressReportService {
    private final StudentProgressReportRepository repository;

    public StudentProgressReportResponse getStudentProgressReport(UUID studentId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("from and to are required");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("from must be before or equal to to");
        }

        StudentProgressReportResponse.StudentIdentity student = repository.findStudentIdentity(studentId);
        if (student == null) {
            throw new NotFoundException("student not found");
        }

        StudentProgressReportResponse.AttendanceSummary attendance = repository.findAttendanceSummary(studentId, from, to);
        List<BigDecimal> scores = repository.findAssessmentScores(studentId, from, to);

        BigDecimal latestScore = scores.isEmpty() ? null : scores.get(scores.size() - 1);
        BigDecimal previousScore = scores.size() < 2 ? null : scores.get(scores.size() - 2);
        BigDecimal averageScore = scores.isEmpty()
                ? null
                : scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);

        StudentProgressReportResponse.Trend trend = StudentProgressReportResponse.Trend.FLAT;
        if (latestScore != null && previousScore != null) {
            int compare = latestScore.compareTo(previousScore);
            if (compare > 0) {
                trend = StudentProgressReportResponse.Trend.UP;
            } else if (compare < 0) {
                trend = StudentProgressReportResponse.Trend.DOWN;
            }
        }

        return new StudentProgressReportResponse(
                student,
                new StudentProgressReportResponse.Period(from, to),
                attendance,
                new StudentProgressReportResponse.SkillProgress(latestScore, averageScore, previousScore, trend),
                repository.findCoachNotes(studentId, from, to)
        );
    }
}
