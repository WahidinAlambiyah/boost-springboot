package com.example.boost.reporting.infrastructure;

import com.example.boost.domain.dto.StudentProgressReportResponse;
import com.example.boost.security.CurrentActor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StudentProgressReportRepository {
    private final JdbcTemplate jdbcTemplate;

    public StudentProgressReportResponse.StudentIdentity findStudentIdentity(UUID studentId) {
        String sql = """
                select s.id,
                       s.student_no,
                       s.full_name,
                       s.nickname,
                       s.date_of_birth,
                       s.current_level,
                       a.name as academy_name,
                       coalesce(string_agg(distinct g.full_name, ', ' order by g.full_name), '') as guardian_names
                from fastworks_springboot.students s
                join fastworks_springboot.academies a on a.id = s.academy_id and a.deleted_at is null
                left join fastworks_springboot.student_guardians sg on sg.student_id = s.id and sg.deleted_at is null
                left join fastworks_springboot.guardians g on g.id = sg.guardian_id and g.deleted_at is null and g.is_active = true
                where s.id = ?
                  and s.deleted_at is null
                group by s.id, s.student_no, s.full_name, s.nickname, s.date_of_birth, s.current_level, a.name
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                return null;
            }
            String guardianNames = rs.getString("guardian_names");
            return new StudentProgressReportResponse.StudentIdentity(
                    rs.getObject("id", UUID.class),
                    rs.getString("student_no"),
                    rs.getString("full_name"),
                    rs.getString("nickname"),
                    rs.getDate("date_of_birth") == null ? null : rs.getDate("date_of_birth").toLocalDate(),
                    null,
                    rs.getString("current_level"),
                    rs.getString("academy_name"),
                    guardianNames == null || guardianNames.isBlank() ? List.of() : Arrays.stream(guardianNames.split(", ")).toList()
            );
        }, studentId);
    }

    public boolean canAccessStudentProgress(CurrentActor actor, UUID studentId) {
        if (actor.hasRole("ADMIN") || actor.hasRole("SUPER_ADMIN") || actor.hasRole("OWNER") || actor.hasRole("ACADEMY_OWNER")) {
            return true;
        }

        String sql = """
                select exists (
                    select 1
                    from fastworks_springboot.students s
                    where s.id = ?
                      and s.deleted_at is null
                      and (
                        exists (
                            select 1
                            from fastworks_springboot.student_guardians sg
                            join fastworks_springboot.guardians g on g.id = sg.guardian_id and g.deleted_at is null
                            join fastworks_springboot.users u on lower(u.email) = lower(g.email) and u.deleted_at is null
                            where sg.student_id = s.id
                              and sg.deleted_at is null
                              and u.id = ?
                        )
                        or exists (
                            select 1
                            from fastworks_springboot.coach_profiles cp
                            where cp.academy_id = s.academy_id
                              and cp.user_id = ?
                              and cp.is_active = true
                              and cp.deleted_at is null
                        )
                        or exists (
                            select 1
                            from fastworks_springboot.enrollments e
                            join fastworks_springboot.class_groups cg on cg.id = e.class_group_id and cg.deleted_at is null
                            where e.student_id = s.id
                              and e.deleted_at is null
                              and cg.instructor_id = ?
                        )
                      )
                )
                """;
        Boolean allowed = jdbcTemplate.queryForObject(sql, Boolean.class, studentId, actor.userId(), actor.userId(), actor.userId());
        return Boolean.TRUE.equals(allowed);
    }

    public StudentProgressReportResponse.AttendanceSummary findAttendanceSummary(UUID studentId, LocalDate from, LocalDate to) {
        String sql = """
                select count(*) as total_sessions,
                       count(*) filter (where upper(ar.attendance_status) = 'PRESENT') as present,
                       count(*) filter (where upper(ar.attendance_status) = 'ABSENT') as absent,
                       count(*) filter (where upper(ar.attendance_status) = 'PERMIT') as permit,
                       count(*) filter (where upper(ar.attendance_status) = 'SICK') as sick,
                       count(*) filter (where upper(ar.attendance_status) = 'LATE') as late
                from fastworks_springboot.attendance_records ar
                join fastworks_springboot.class_sessions cs on cs.id = ar.class_session_id and cs.deleted_at is null
                where ar.deleted_at is null
                  and ar.student_id = ?
                  and cs.session_date between ? and ?
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new StudentProgressReportResponse.AttendanceSummary(
                        rs.getLong("total_sessions"),
                        rs.getLong("present"),
                        rs.getLong("absent"),
                        rs.getLong("permit"),
                        rs.getLong("sick"),
                        rs.getLong("late"),
                        null),
                studentId,
                Date.valueOf(from),
                Date.valueOf(to)
        );
    }

    public List<StudentProgressReportResponse.AttendanceTimelineItem> findAttendanceTimeline(UUID studentId, LocalDate from, LocalDate to) {
        String sql = """
                select cs.id as session_id,
                       cs.session_date,
                       cs.start_time,
                       cs.end_time,
                       coalesce(cg.name, cg.code) as class_group_name,
                       al.name as location_name,
                       ar.attendance_status as status,
                       ar.remarks
                from fastworks_springboot.attendance_records ar
                join fastworks_springboot.class_sessions cs on cs.id = ar.class_session_id and cs.deleted_at is null
                join fastworks_springboot.class_groups cg on cg.id = cs.class_group_id and cg.deleted_at is null
                left join fastworks_springboot.academy_locations al on al.id = cs.location_id and al.deleted_at is null
                where ar.deleted_at is null
                  and ar.student_id = ?
                  and cs.session_date between ? and ?
                order by cs.session_date asc, cs.start_time asc
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new StudentProgressReportResponse.AttendanceTimelineItem(
                rs.getObject("session_id", UUID.class),
                rs.getDate("session_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime(),
                rs.getString("class_group_name"),
                rs.getString("location_name"),
                rs.getString("status"),
                rs.getString("remarks")
        ), studentId, Date.valueOf(from), Date.valueOf(to));
    }

    public List<StudentProgressReportResponse.SkillProgress> findSkillProgress(UUID studentId, LocalDate from, LocalDate to) {
        String sql = """
                with ranked as (
                  select ask.code as skill_code,
                         ask.name as skill_name,
                         ask.max_score,
                         sas.score,
                         sas.notes,
                         row_number() over (partition by ask.id order by sa.assessed_at desc, sa.created_at desc) as rn,
                         avg(sas.score) over (partition by ask.id) as average_score
                  from fastworks_springboot.student_assessment_scores sas
                  join fastworks_springboot.student_assessments sa on sa.id = sas.assessment_id and sa.deleted_at is null
                  join fastworks_springboot.assessment_skills ask on ask.id = sas.skill_id and ask.deleted_at is null
                  where sas.deleted_at is null
                    and sa.student_id = ?
                    and sa.assessed_at >= ?::date
                    and sa.assessed_at < (?::date + interval '1 day')
                )
                select cur.skill_code,
                       cur.skill_name,
                       cur.score as latest_score,
                       cur.average_score,
                       prev.score as previous_score,
                       cur.max_score,
                       cur.notes as latest_notes
                from ranked cur
                left join ranked prev on prev.skill_code = cur.skill_code and prev.rn = 2
                where cur.rn = 1
                order by cur.skill_name asc
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new StudentProgressReportResponse.SkillProgress(
                rs.getString("skill_code"),
                rs.getString("skill_name"),
                rs.getBigDecimal("latest_score"),
                rs.getBigDecimal("average_score"),
                rs.getBigDecimal("previous_score"),
                null,
                rs.getObject("max_score", Integer.class),
                rs.getString("latest_notes")
        ), studentId, Date.valueOf(from), Date.valueOf(to));
    }

    public List<StudentProgressReportResponse.CoachNote> findCoachNotes(UUID studentId, LocalDate from, LocalDate to) {
        String sql = """
                select sa.id as assessment_id,
                       coalesce(cs.session_date, sa.assessed_at::date) as session_date,
                       cp.full_name as coach_name,
                       sa.notes as overall_notes,
                       null::text as recommendation
                from fastworks_springboot.student_assessments sa
                left join fastworks_springboot.class_sessions cs on cs.id = sa.class_session_id and cs.deleted_at is null
                left join fastworks_springboot.coach_profiles cp on cp.id = sa.coach_id and cp.deleted_at is null
                where sa.deleted_at is null
                  and sa.student_id = ?
                  and sa.assessed_at >= ?::date
                  and sa.assessed_at < (?::date + interval '1 day')
                  and sa.notes is not null
                  and trim(sa.notes) <> ''
                order by sa.assessed_at desc
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new StudentProgressReportResponse.CoachNote(
                        rs.getObject("assessment_id", UUID.class),
                        rs.getDate("session_date").toLocalDate(),
                        rs.getString("coach_name"),
                        rs.getString("overall_notes"),
                        rs.getString("recommendation")
                ),
                studentId,
                Date.valueOf(from),
                Date.valueOf(to)
        );
    }

    public List<StudentProgressReportResponse.UpcomingSession> findUpcomingSessions(UUID studentId, LocalDate from) {
        String sql = """
                select cs.id as session_id,
                       cs.session_date,
                       cs.start_time,
                       cs.end_time,
                       coalesce(cg.name, cg.code) as class_group_name,
                       al.name as location_name
                from fastworks_springboot.enrollments e
                join fastworks_springboot.class_sessions cs on cs.class_group_id = e.class_group_id and cs.deleted_at is null
                join fastworks_springboot.class_groups cg on cg.id = cs.class_group_id and cg.deleted_at is null
                left join fastworks_springboot.academy_locations al on al.id = cs.location_id and al.deleted_at is null
                where e.student_id = ?
                  and e.deleted_at is null
                  and upper(e.status) = 'ACTIVE'
                  and cs.session_date >= ?
                  and coalesce(upper(cs.status), 'SCHEDULED') not in ('CANCELLED', 'ARCHIVED')
                order by cs.session_date asc, cs.start_time asc
                limit 10
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new StudentProgressReportResponse.UpcomingSession(
                rs.getObject("session_id", UUID.class),
                rs.getDate("session_date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime(),
                rs.getString("class_group_name"),
                rs.getString("location_name")
        ), studentId, Date.valueOf(from));
    }
}
