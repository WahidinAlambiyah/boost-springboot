package com.example.boost.dashboard.infrastructure;

import com.example.boost.domain.dto.DashboardCommonResponses;
import com.example.boost.domain.dto.DashboardCommonResponses.AcademySummary;
import com.example.boost.domain.dto.DashboardCommonResponses.CoachSummary;
import com.example.boost.domain.dto.DashboardCommonResponses.CoachTodaySession;
import com.example.boost.domain.dto.DashboardCommonResponses.LatestProgress;
import com.example.boost.domain.dto.DashboardCommonResponses.OwnerTodaySession;
import com.example.boost.domain.dto.DashboardCommonResponses.ParentAttendanceSummary;
import com.example.boost.domain.dto.DashboardCommonResponses.ParentStudentSummary;
import com.example.boost.domain.dto.DashboardCommonResponses.PendingAssessmentStudent;
import com.example.boost.domain.dto.DashboardCommonResponses.RecentAssessment;
import com.example.boost.domain.dto.DashboardCommonResponses.SkillProgress;
import com.example.boost.domain.dto.DashboardCommonResponses.StudentNeedAttention;
import com.example.boost.domain.dto.DashboardCommonResponses.UpcomingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {
    private final JdbcTemplate jdbcTemplate;

    public Optional<AcademySummary> findAcademy(UUID academyId) {
        String sql = """
                select id, name, code
                from fastworks_springboot.academies
                where id = ? and deleted_at is null
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AcademySummary(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getString("code")
        ), academyId).stream().findFirst();
    }

    public long countActiveStudents(UUID academyId) {
        return count("""
                select count(*)
                from fastworks_springboot.students
                where academy_id = ? and is_active = true and deleted_at is null
                """, academyId);
    }

    public long countActiveCoaches(UUID academyId) {
        return count("""
                select count(*)
                from fastworks_springboot.coach_profiles
                where academy_id = ? and is_active = true and deleted_at is null
                """, academyId);
    }

    public long countTodaySessions(UUID academyId, LocalDate today) {
        return count("""
                select count(*)
                from fastworks_springboot.class_sessions
                where academy_id = ? and session_date = ? and deleted_at is null
                """, academyId, Date.valueOf(today));
    }

    public BigDecimal attendanceRate(UUID academyId, LocalDate from, LocalDate to) {
        String sql = """
                select count(*) as total,
                       count(*) filter (where upper(ar.attendance_status) = 'PRESENT') as present
                from fastworks_springboot.attendance_records ar
                join fastworks_springboot.class_sessions cs on cs.id = ar.class_session_id and cs.deleted_at is null
                where cs.academy_id = ?
                  and cs.session_date between ? and ?
                  and ar.deleted_at is null
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rate(rs.getLong("present"), rs.getLong("total")),
                academyId, Date.valueOf(from), Date.valueOf(to));
    }

    public long countAssessedActiveStudents(UUID academyId, LocalDate from, LocalDate to) {
        return count("""
                select count(distinct sa.student_id)
                from fastworks_springboot.student_assessments sa
                join fastworks_springboot.students s on s.id = sa.student_id and s.is_active = true and s.deleted_at is null
                where sa.academy_id = ?
                  and sa.assessed_at >= ?::date
                  and sa.assessed_at < (?::date + interval '1 day')
                  and sa.deleted_at is null
                """, academyId, Date.valueOf(from), Date.valueOf(to));
    }

    public long countUnpaidInvoices(UUID academyId) {
        return count("""
                select count(*)
                from fastworks_springboot.invoices
                where academy_id = ?
                  and upper(status) in ('OPEN','PARTIALLY_PAID','OVERDUE','UNPAID')
                  and deleted_at is null
                """, academyId);
    }

    public String currentMonthPayrollStatus(UUID academyId, int month, int year) {
        String sql = """
                select status
                from fastworks_springboot.coach_payroll_periods
                where academy_id = ? and period_month = ? and period_year = ? and deleted_at is null
                order by created_at desc
                limit 1
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("status"), academyId, month, year)
                .stream().findFirst().orElse("NOT_GENERATED");
    }

    public List<OwnerTodaySession> findOwnerTodaySessions(UUID academyId, LocalDate today) {
        String sql = """
                select cs.id,
                       coalesce(cs.topic, cg.name, cg.code, 'Class Session') as title,
                       cs.session_date,
                       cs.start_time,
                       cs.end_time,
                       al.name as location_name,
                       coalesce(cg.name, cg.code) as class_group_name,
                       coalesce(string_agg(distinct cp.full_name, ', ' order by cp.full_name), '') as coach_names,
                       cs.status,
                       case
                         when count(distinct ar.student_id) = 0 then 'NOT_SUBMITTED'
                         when count(distinct ar.student_id) < count(distinct e.student_id) then 'PARTIAL'
                         else 'SUBMITTED'
                       end as attendance_status
                from fastworks_springboot.class_sessions cs
                join fastworks_springboot.class_groups cg on cg.id = cs.class_group_id and cg.deleted_at is null
                left join fastworks_springboot.academy_locations al on al.id = cs.location_id and al.deleted_at is null
                left join fastworks_springboot.class_session_coaches csc on csc.class_session_id = cs.id and csc.deleted_at is null
                left join fastworks_springboot.coach_profiles cp on cp.id = csc.coach_id and cp.deleted_at is null
                left join fastworks_springboot.enrollments e on e.class_group_id = cs.class_group_id and e.deleted_at is null and upper(e.status) = 'ACTIVE'
                left join fastworks_springboot.attendance_records ar on ar.class_session_id = cs.id and ar.student_id = e.student_id and ar.deleted_at is null
                where cs.academy_id = ? and cs.session_date = ? and cs.deleted_at is null
                group by cs.id, cs.topic, cg.name, cg.code, cs.session_date, cs.start_time, cs.end_time, al.name, cs.status
                order by cs.start_time asc
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new OwnerTodaySession(
                rs.getObject("id", UUID.class), rs.getString("title"), rs.getDate("session_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(), rs.getTime("end_time").toLocalTime(),
                rs.getString("location_name"), rs.getString("class_group_name"), rs.getString("coach_names"),
                rs.getString("status"), rs.getString("attendance_status")
        ), academyId, Date.valueOf(today));
    }

    public List<StudentNeedAttention> findStudentsWithoutAssessment(UUID academyId, LocalDate from, LocalDate to) {
        String sql = """
                select s.id, s.full_name, s.nickname, s.current_level,
                       max(cs.session_date) filter (where ar.id is not null) as last_attendance_date,
                       max(sa_all.assessed_at)::date as last_assessment_date
                from fastworks_springboot.students s
                left join fastworks_springboot.attendance_records ar on ar.student_id = s.id and ar.deleted_at is null
                left join fastworks_springboot.class_sessions cs on cs.id = ar.class_session_id and cs.deleted_at is null
                left join fastworks_springboot.student_assessments sa_all on sa_all.student_id = s.id and sa_all.deleted_at is null
                where s.academy_id = ? and s.is_active = true and s.deleted_at is null
                  and not exists (
                    select 1 from fastworks_springboot.student_assessments sa
                    where sa.student_id = s.id and sa.academy_id = s.academy_id
                      and sa.assessed_at >= ?::date and sa.assessed_at < (?::date + interval '1 day')
                      and sa.deleted_at is null
                  )
                group by s.id, s.full_name, s.nickname, s.current_level
                order by s.full_name asc
                limit 20
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new StudentNeedAttention(
                rs.getObject("id", UUID.class), rs.getString("full_name"), rs.getString("nickname"),
                rs.getString("current_level"), "NO_ASSESSMENT_IN_PERIOD",
                nullableDate(rs.getDate("last_attendance_date")), nullableDate(rs.getDate("last_assessment_date"))
        ), academyId, Date.valueOf(from), Date.valueOf(to));
    }

    public List<RecentAssessment> findRecentAssessments(UUID academyId, LocalDate from, LocalDate to) {
        String sql = """
                select sa.id as assessment_id, sa.student_id, s.full_name as student_name, sa.class_session_id,
                       cs.session_date, cp.full_name as coach_name, sa.notes as overall_notes, null::text as recommendation
                from fastworks_springboot.student_assessments sa
                join fastworks_springboot.students s on s.id = sa.student_id and s.deleted_at is null
                left join fastworks_springboot.class_sessions cs on cs.id = sa.class_session_id and cs.deleted_at is null
                left join fastworks_springboot.coach_profiles cp on cp.id = sa.coach_id and cp.deleted_at is null
                where sa.academy_id = ? and sa.assessed_at >= ?::date and sa.assessed_at < (?::date + interval '1 day')
                  and sa.deleted_at is null
                order by sa.assessed_at desc
                limit 10
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new RecentAssessment(
                rs.getObject("assessment_id", UUID.class), rs.getObject("student_id", UUID.class), rs.getString("student_name"),
                rs.getObject("class_session_id", UUID.class), nullableDate(rs.getDate("session_date")), rs.getString("coach_name"),
                rs.getString("overall_notes"), rs.getString("recommendation")
        ), academyId, Date.valueOf(from), Date.valueOf(to));
    }

    public Optional<CoachSummary> findCoachByUserId(UUID userId, UUID academyId) {
        String sql = """
                select id, user_id, full_name, academy_id
                from fastworks_springboot.coach_profiles
                where user_id = ? and academy_id = ? and is_active = true and deleted_at is null
                limit 1
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new CoachSummary(
                rs.getObject("id", UUID.class), rs.getObject("user_id", UUID.class), rs.getString("full_name"), rs.getObject("academy_id", UUID.class)
        ), userId, academyId).stream().findFirst();
    }

    public List<CoachTodaySession> findCoachTodaySessions(UUID academyId, UUID coachId, LocalDate today) {
        String sql = """
                select cs.id, coalesce(cs.topic, cg.name, cg.code, 'Class Session') as title, cs.session_date, cs.start_time, cs.end_time,
                       al.name as location_name, coalesce(cg.name, cg.code) as class_group_name,
                       count(distinct e.student_id) as student_count,
                       count(distinct ar.student_id) >= count(distinct e.student_id) and count(distinct e.student_id) > 0 as attendance_submitted,
                       count(distinct sa.student_id) as assessment_completed_count,
                       cs.status
                from fastworks_springboot.class_sessions cs
                join fastworks_springboot.class_session_coaches csc on csc.class_session_id = cs.id and csc.deleted_at is null
                join fastworks_springboot.class_groups cg on cg.id = cs.class_group_id and cg.deleted_at is null
                left join fastworks_springboot.academy_locations al on al.id = cs.location_id and al.deleted_at is null
                left join fastworks_springboot.enrollments e on e.class_group_id = cs.class_group_id and e.deleted_at is null and upper(e.status) = 'ACTIVE'
                left join fastworks_springboot.attendance_records ar on ar.class_session_id = cs.id and ar.student_id = e.student_id and ar.deleted_at is null
                left join fastworks_springboot.student_assessments sa on sa.class_session_id = cs.id and sa.student_id = e.student_id and sa.deleted_at is null
                where cs.academy_id = ? and csc.coach_id = ? and cs.session_date = ? and cs.deleted_at is null
                group by cs.id, cs.topic, cg.name, cg.code, cs.session_date, cs.start_time, cs.end_time, al.name, cs.status
                order by cs.start_time asc
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new CoachTodaySession(
                rs.getObject("id", UUID.class), rs.getString("title"), rs.getDate("session_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(), rs.getTime("end_time").toLocalTime(), rs.getString("location_name"),
                rs.getString("class_group_name"), rs.getLong("student_count"), rs.getBoolean("attendance_submitted"),
                rs.getLong("assessment_completed_count"), rs.getString("status")
        ), academyId, coachId, Date.valueOf(today));
    }

    public long countCoachPendingAttendance(UUID academyId, UUID coachId, LocalDate from, LocalDate to) {
        return count("""
                select count(*) from (
                  select cs.id
                  from fastworks_springboot.class_sessions cs
                  join fastworks_springboot.class_session_coaches csc on csc.class_session_id = cs.id and csc.deleted_at is null
                  left join fastworks_springboot.enrollments e on e.class_group_id = cs.class_group_id and e.deleted_at is null and upper(e.status) = 'ACTIVE'
                  left join fastworks_springboot.attendance_records ar on ar.class_session_id = cs.id and ar.student_id = e.student_id and ar.deleted_at is null
                  where cs.academy_id = ? and csc.coach_id = ? and cs.session_date between ? and ? and cs.deleted_at is null
                  group by cs.id
                  having count(distinct ar.student_id) < count(distinct e.student_id)
                ) pending
                """, academyId, coachId, Date.valueOf(from), Date.valueOf(to));
    }

    public List<PendingAssessmentStudent> findCoachPendingAssessmentStudents(UUID academyId, UUID coachId, LocalDate from, LocalDate to) {
        String sql = """
                select distinct s.id as student_id, s.full_name, s.nickname, cs.id as class_session_id, cs.session_date, s.current_level
                from fastworks_springboot.class_sessions cs
                join fastworks_springboot.class_session_coaches csc on csc.class_session_id = cs.id and csc.deleted_at is null
                join fastworks_springboot.enrollments e on e.class_group_id = cs.class_group_id and e.deleted_at is null and upper(e.status) = 'ACTIVE'
                join fastworks_springboot.students s on s.id = e.student_id and s.deleted_at is null
                where cs.academy_id = ? and csc.coach_id = ? and cs.session_date between ? and ? and cs.deleted_at is null
                  and not exists (
                    select 1 from fastworks_springboot.student_assessments sa
                    where sa.class_session_id = cs.id and sa.student_id = s.id and sa.coach_id = ? and sa.deleted_at is null
                  )
                order by cs.session_date desc, s.full_name asc
                limit 50
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new PendingAssessmentStudent(
                rs.getObject("student_id", UUID.class), rs.getString("full_name"), rs.getString("nickname"),
                rs.getObject("class_session_id", UUID.class), rs.getDate("session_date").toLocalDate(), rs.getString("current_level")
        ), academyId, coachId, Date.valueOf(from), Date.valueOf(to), coachId);
    }

    public long countCoachCompletedAssessments(UUID academyId, UUID coachId, LocalDate from, LocalDate to) {
        return count("""
                select count(*)
                from fastworks_springboot.student_assessments
                where academy_id = ? and coach_id = ? and assessed_at >= ?::date and assessed_at < (?::date + interval '1 day') and deleted_at is null
                """, academyId, coachId, Date.valueOf(from), Date.valueOf(to));
    }

    public Optional<ParentStudentSummary> findParentStudent(UUID studentId) {
        String sql = """
                select s.id, s.student_no, s.full_name, s.nickname, s.current_level, a.name as academy_name
                from fastworks_springboot.students s
                join fastworks_springboot.academies a on a.id = s.academy_id and a.deleted_at is null
                where s.id = ? and s.deleted_at is null
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ParentStudentSummary(
                rs.getObject("id", UUID.class), rs.getString("student_no"), rs.getString("full_name"), rs.getString("nickname"),
                rs.getString("current_level"), rs.getString("academy_name")
        ), studentId).stream().findFirst();
    }

    public ParentAttendanceSummary parentAttendanceSummary(UUID studentId, LocalDate from, LocalDate to) {
        String sql = """
                select count(*) as total,
                       count(*) filter (where upper(attendance_status) = 'PRESENT') as present,
                       count(*) filter (where upper(attendance_status) = 'ABSENT') as absent,
                       count(*) filter (where upper(attendance_status) = 'PERMIT') as permit,
                       count(*) filter (where upper(attendance_status) = 'SICK') as sick,
                       count(*) filter (where upper(attendance_status) = 'LATE') as late
                from fastworks_springboot.attendance_records ar
                join fastworks_springboot.class_sessions cs on cs.id = ar.class_session_id and cs.deleted_at is null
                where ar.student_id = ? and cs.session_date between ? and ? and ar.deleted_at is null
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new ParentAttendanceSummary(
                rs.getLong("total"), rs.getLong("present"), rs.getLong("absent"), rs.getLong("permit"), rs.getLong("sick"),
                rs.getLong("late"), rate(rs.getLong("present"), rs.getLong("total"))
        ), studentId, Date.valueOf(from), Date.valueOf(to));
    }

    public Optional<LatestProgress> latestProgress(UUID studentId) {
        String sql = """
                select sa.assessed_at::date as latest_assessment_date, cp.full_name as coach_name, sa.notes as overall_notes, null::text as recommendation
                from fastworks_springboot.student_assessments sa
                left join fastworks_springboot.coach_profiles cp on cp.id = sa.coach_id and cp.deleted_at is null
                where sa.student_id = ? and sa.deleted_at is null
                order by sa.assessed_at desc
                limit 1
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new LatestProgress(
                nullableDate(rs.getDate("latest_assessment_date")), rs.getString("coach_name"), rs.getString("overall_notes"), rs.getString("recommendation")
        ), studentId).stream().findFirst();
    }

    public List<SkillProgress> skillProgress(UUID studentId) {
        String sql = """
                with ranked as (
                  select ask.code, ask.name, sas.score, sa.assessed_at,
                         row_number() over (partition by ask.id order by sa.assessed_at desc) rn,
                         avg(sas.score) over (partition by ask.id) avg_score
                  from fastworks_springboot.student_assessment_scores sas
                  join fastworks_springboot.student_assessments sa on sa.id = sas.assessment_id and sa.deleted_at is null
                  join fastworks_springboot.assessment_skills ask on ask.id = sas.skill_id and ask.deleted_at is null
                  where sa.student_id = ? and sas.deleted_at is null
                )
                select cur.code, cur.name, cur.score as latest_score, cur.avg_score as average_score, prev.score as previous_score
                from ranked cur
                left join ranked prev on prev.code = cur.code and prev.rn = 2
                where cur.rn = 1
                order by cur.name asc
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            BigDecimal latest = rs.getBigDecimal("latest_score");
            BigDecimal previous = rs.getBigDecimal("previous_score");
            String trend = previous == null || latest == null ? "FLAT" : latest.compareTo(previous) > 0 ? "UP" : latest.compareTo(previous) < 0 ? "DOWN" : "FLAT";
            return new SkillProgress(rs.getString("code"), rs.getString("name"), latest, rs.getBigDecimal("average_score"), previous, trend);
        }, studentId);
    }

    public List<UpcomingSession> upcomingSessions(UUID studentId, LocalDate from) {
        String sql = """
                select cs.id, cs.session_date, cs.start_time, cs.end_time, al.name as location_name, coalesce(cg.name, cg.code) as class_group_name, cs.status
                from fastworks_springboot.enrollments e
                join fastworks_springboot.class_sessions cs on cs.class_group_id = e.class_group_id and cs.deleted_at is null
                join fastworks_springboot.class_groups cg on cg.id = cs.class_group_id and cg.deleted_at is null
                left join fastworks_springboot.academy_locations al on al.id = cs.location_id and al.deleted_at is null
                where e.student_id = ? and e.deleted_at is null and upper(e.status) = 'ACTIVE' and cs.session_date >= ?
                order by cs.session_date asc, cs.start_time asc
                limit 10
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new UpcomingSession(
                rs.getObject("id", UUID.class), rs.getDate("session_date").toLocalDate(), rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime(), rs.getString("location_name"), rs.getString("class_group_name"), rs.getString("status")
        ), studentId, Date.valueOf(from));
    }

    private long count(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    private static BigDecimal rate(long present, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(present).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private static LocalDate nullableDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
