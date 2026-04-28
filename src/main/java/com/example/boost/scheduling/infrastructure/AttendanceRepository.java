package com.example.boost.scheduling.infrastructure;

import com.example.boost.domain.dto.AttendanceSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AttendanceRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<AttendanceSummaryResponse> findSummaries(UUID actorUserId, boolean guardianScope, boolean instructorScope) {
        String sql = """
                select ar.id::text as code,
                       s.full_name as name,
                       ar.attendance_status as status
                from fastworks_springboot.attendance_records ar
                join fastworks_springboot.students s on s.id = ar.student_id and s.deleted_at is null
                join fastworks_springboot.class_sessions cs on cs.id = ar.class_session_id and cs.deleted_at is null
                where ar.deleted_at is null
                  and (
                    ? = false
                    or exists (
                        select 1
                        from fastworks_springboot.student_guardians sg
                        join fastworks_springboot.guardians g on g.id = sg.guardian_id and g.deleted_at is null
                        join fastworks_springboot.users u on lower(u.email) = lower(g.email) and u.deleted_at is null
                        where u.id = ?
                          and sg.student_id = ar.student_id
                          and sg.deleted_at is null
                    )
                  )
                  and (
                    ? = false
                    or cs.instructor_id = ?
                  )
                order by ar.created_at desc
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> AttendanceSummaryResponse.builder()
                        .code(rs.getString("code"))
                        .name(rs.getString("name"))
                        .status(rs.getString("status"))
                        .build(),
                guardianScope,
                actorUserId,
                instructorScope,
                actorUserId
        );
    }
}
