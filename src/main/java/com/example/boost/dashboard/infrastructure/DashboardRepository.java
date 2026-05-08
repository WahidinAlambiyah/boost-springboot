package com.example.boost.dashboard.infrastructure;

import com.example.boost.domain.dto.DashboardCommonResponses;
import com.example.boost.domain.dto.DashboardCommonResponses.AcademySummary;
import com.example.boost.domain.dto.DashboardCommonResponses.CoachSummary;
import com.example.boost.domain.dto.DashboardCommonResponses.CoachTodaySession;
import com.example.boost.domain.dto.DashboardCommonResponses.LatestProgress;
import com.example.boost.domain.dto.DashboardCommonResponses.LevelDistributionItem;
import com.example.boost.domain.dto.DashboardCommonResponses.FinanceSummary;
import com.example.boost.domain.dto.DashboardCommonResponses.OwnerTodaySession;
import com.example.boost.domain.dto.DashboardCommonResponses.AttendanceTrendItem;
import com.example.boost.domain.dto.DashboardCommonResponses.PackageSummary;
import com.example.boost.domain.dto.DashboardCommonResponses.ParentAttendanceSummary;
import com.example.boost.domain.dto.DashboardCommonResponses.ParentStudentSummary;
import com.example.boost.domain.dto.DashboardCommonResponses.PendingAssessmentStudent;
import com.example.boost.domain.dto.DashboardCommonResponses.PayrollSummary;
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
import java.time.YearMonth;
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

    public Optional<UUID> findFirstActiveAcademyId() {
        String sql = """
                select id
                from fastworks_springboot.academies
                where is_active = true and deleted_at is null
                order by name asc
                limit 1
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getObject("id", UUID.class)).stream().findFirst();
    }

    public List<UUID> findAccessibleAcademyIds(UUID userId, int limit) {
        String sql = """
                with accessible as (
                    select cp.academy_id
                    from fastworks_springboot.coach_profiles cp
                    where cp.user_id = ?
                      and cp.is_active = true
                      and cp.deleted_at is null

                    union

                    select g.academy_id
                    from fastworks_springboot.guardians g
                    join fastworks_springboot.users u on lower(u.email) = lower(g.email) and u.deleted_at is null
                    where u.id = ?
                      and g.is_active = true
                      and g.deleted_at is null

                    union

                    select cg.academy_id
                    from fastworks_springboot.class_groups cg
                    where cg.instructor_id = ?
                      and cg.deleted_at is null
                )
                select a.id
                from fastworks_springboot.academies a
                join accessible acc on acc.academy_id = a.id
                where a.is_active = true
                  and a.deleted_at is null
                order by a.name asc
                limit ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getObject("id", UUID.class), userId, userId, userId, limit);
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

    public List<AttendanceTrendItem> attendanceTrend(UUID academyId, LocalDate from, LocalDate to) {
        String sql = """
                with trend_dates as (
                    select generate_series(?::date, ?::date, interval '1 day')::date as trend_date
                )
                select td.trend_date as date,
                       count(ar.id) filter (where upper(ar.attendance_status) = 'PRESENT') as present,
                       count(ar.id) filter (where upper(ar.attendance_status) = 'ABSENT') as absent,
                       count(ar.id) filter (where upper(ar.attendance_status) = 'PERMIT') as permit,
                       count(ar.id) filter (where upper(ar.attendance_status) = 'SICK') as sick,
                       count(ar.id) filter (where upper(ar.attendance_status) = 'LATE') as late,
                       count(ar.id) as total
                from trend_dates td
                left join fastworks_springboot.class_sessions cs
                       on cs.academy_id = ?
                      and cs.session_date = td.trend_date
                      and cs.deleted_at is null
                left join fastworks_springboot.attendance_records ar
                       on ar.class_session_id = cs.id
                      and ar.deleted_at is null
                group by td.trend_date
                order by td.trend_date asc
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AttendanceTrendItem(
                rs.getDate("date").toLocalDate(),
                rs.getLong("present"),
                rs.getLong("absent"),
                rs.getLong("permit"),
                rs.getLong("sick"),
                rs.getLong("late"),
                rs.getLong("total")
        ), Date.valueOf(from), Date.valueOf(to), academyId);
    }

    public List<LevelDistributionItem> levelDistribution(UUID academyId) {
        String sql = """
                select coalesce(nullif(trim(s.current_level), ''), 'UNKNOWN') as level,
                       count(*) as total_students
                from fastworks_springboot.students s
                where s.academy_id = ?
                  and s.is_active = true
                  and s.deleted_at is null
                group by coalesce(nullif(trim(s.current_level), ''), 'UNKNOWN')
                order by level asc
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new LevelDistributionItem(
                rs.getString("level"),
                rs.getLong("total_students")
        ), academyId);
    }

    public PackageSummary packageSummary(UUID academyId, LocalDate today) {
        String sql = """
                select count(*) filter (where upper(sps.status) = 'ACTIVE') as active_subscriptions,
                       count(*) filter (
                           where upper(sps.status) = 'ACTIVE'
                             and sps.end_date is not null
                             and sps.end_date between ? and ?
                       ) as expiring_soon,
                       count(*) filter (
                           where upper(sps.status) = 'ACTIVE'
                             and sps.remaining_sessions is not null
                             and sps.remaining_sessions <= 3
                       ) as low_remaining_sessions
                from fastworks_springboot.student_package_subscriptions sps
                where sps.academy_id = ?
                  and sps.deleted_at is null
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new PackageSummary(
                rs.getLong("active_subscriptions"),
                rs.getLong("expiring_soon"),
                rs.getLong("low_remaining_sessions")
        ), Date.valueOf(today), Date.valueOf(today.plusDays(7)), academyId);
    }

    public FinanceSummary financeSummary(UUID academyId, LocalDate from, LocalDate to, LocalDate today) {
        String sql = """
                select coalesce(sum(i.paid_amount), 0) as paid_amount,
                       coalesce(sum(greatest(i.total_amount - i.paid_amount, 0)), 0) as unpaid_amount,
                       coalesce(sum(
                           case
                               when i.due_date < ? and upper(i.status) in ('OPEN', 'PARTIALLY_PAID', 'OVERDUE', 'UNPAID')
                                   then greatest(i.total_amount - i.paid_amount, 0)
                               else 0
                           end
                       ), 0) as overdue_amount,
                       count(*) as total_invoices
                from fastworks_springboot.invoices i
                where i.academy_id = ?
                  and i.issue_date between ? and ?
                  and i.deleted_at is null
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new FinanceSummary(
                zeroIfNull(rs.getBigDecimal("paid_amount")),
                zeroIfNull(rs.getBigDecimal("unpaid_amount")),
                zeroIfNull(rs.getBigDecimal("overdue_amount")),
                rs.getLong("total_invoices")
        ), Date.valueOf(today), academyId, Date.valueOf(from), Date.valueOf(to));
    }

    public PayrollSummary payrollSummary(UUID academyId, YearMonth periodMonth) {
        String sql = """
                select cpp.period_month,
                       cpp.period_year,
                       cpp.status,
                       count(distinct cpi.coach_id) as total_coaches,
                       coalesce(sum(cpi.total_amount), 0) as total_amount
                from fastworks_springboot.coach_payroll_periods cpp
                left join fastworks_springboot.coach_payroll_items cpi
                       on cpi.payroll_period_id = cpp.id
                      and cpi.deleted_at is null
                where cpp.academy_id = ?
                  and cpp.period_month = ?
                  and cpp.period_year = ?
                  and cpp.deleted_at is null
                group by cpp.id, cpp.period_month, cpp.period_year, cpp.status, cpp.created_at
                order by cpp.created_at desc
                limit 1
                """;

        List<PayrollSummary> results = jdbcTemplate.query(sql, (rs, rowNum) -> new PayrollSummary(
                rs.getInt("period_month"),
                rs.getInt("period_year"),
                rs.getString("status"),
                rs.getLong("total_coaches"),
                zeroIfNull(rs.getBigDecimal("total_amount"))
        ), academyId, periodMonth.getMonthValue(), periodMonth.getYear());

        if (results.isEmpty()) {
            return new PayrollSummary(
                    periodMonth.getMonthValue(),
                    periodMonth.getYear(),
                    "NOT_GENERATED",
                    0L,
                    BigDecimal.ZERO.setScale(2)
            );
        }

        return results.get(0);
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

    private static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value;
    }

    private static LocalDate nullableDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
