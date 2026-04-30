package com.example.boost.reporting.infrastructure;

import com.example.boost.domain.dto.StudentProgressReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StudentProgressReportRepository {
    private final JdbcTemplate jdbcTemplate;

    public StudentProgressReportResponse.StudentIdentity findStudentIdentity(UUID studentId) {
        String sql = """
                select s.id, s.student_no, s.full_name, s.nickname, s.current_level
                from fastworks_springboot.students s
                where s.id = ?
                  and s.deleted_at is null
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                return null;
            }
            return new StudentProgressReportResponse.StudentIdentity(
                    rs.getObject("id", UUID.class),
                    rs.getString("student_no"),
                    rs.getString("full_name"),
                    rs.getString("nickname"),
                    rs.getString("current_level")
            );
        }, studentId);
    }

    public StudentProgressReportResponse.AttendanceSummary findAttendanceSummary(UUID studentId, LocalDate from, LocalDate to) {
        String sql = """
                select count(*) as total_sessions,
                       sum(case when ar.attendance_status = 'PRESENT' then 1 else 0 end) as present,
                       sum(case when ar.attendance_status = 'PERMIT' then 1 else 0 end) as permit,
                       sum(case when ar.attendance_status = 'ABSENT' then 1 else 0 end) as absent
                from fastworks_springboot.attendance_records ar
                join fastworks_springboot.class_sessions cs on cs.id = ar.class_session_id and cs.deleted_at is null
                where ar.deleted_at is null
                  and ar.student_id = ?
                  and cs.session_date between ? and ?
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new StudentProgressReportResponse.AttendanceSummary(
                        rs.getInt("total_sessions"),
                        rs.getInt("present"),
                        rs.getInt("permit"),
                        rs.getInt("absent")),
                studentId,
                Date.valueOf(from),
                Date.valueOf(to)
        );
    }

    public List<BigDecimal> findAssessmentScores(UUID studentId, LocalDate from, LocalDate to) {
        String sql = """
                select avg(ss.score) as score
                from fastworks_springboot.student_assessments sa
                join fastworks_springboot.student_assessment_scores ss on ss.assessment_id = sa.id and ss.deleted_at is null
                where sa.deleted_at is null
                  and sa.student_id = ?
                  and sa.assessed_at::date between ? and ?
                group by sa.id, sa.assessed_at
                order by sa.assessed_at asc, sa.created_at asc
                """;
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getBigDecimal("score"),
                studentId,
                Date.valueOf(from),
                Date.valueOf(to)
        );
    }

    public List<StudentProgressReportResponse.CoachNoteBySessionDate> findCoachNotes(UUID studentId, LocalDate from, LocalDate to) {
        String sql = """
                select sa.assessed_at::date as session_date, sa.notes
                from fastworks_springboot.student_assessments sa
                where sa.deleted_at is null
                  and sa.student_id = ?
                  and sa.assessed_at::date between ? and ?
                  and sa.notes is not null
                  and trim(sa.notes) <> ''
                order by sa.assessed_at asc
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new StudentProgressReportResponse.CoachNoteBySessionDate(
                        rs.getDate("session_date").toLocalDate(),
                        rs.getString("notes")
                ),
                studentId,
                Date.valueOf(from),
                Date.valueOf(to)
        );
    }
}
