package com.example.boost.scheduling.infrastructure;

import com.example.boost.domain.dto.SchedulingSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SchedulingRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<SchedulingSummaryResponse> findSummaries(UUID actorUserId, boolean instructorScope) {
        String sql = """
                select cg.id::text as code,
                       cg.name as name,
                       cg.status as status
                from fastworks_springboot.class_groups cg
                where cg.deleted_at is null
                  and (
                    ? = false
                    or cg.instructor_id = ?
                  )
                order by cg.updated_at desc
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> SchedulingSummaryResponse.builder()
                        .code(rs.getString("code"))
                        .name(rs.getString("name"))
                        .status(rs.getString("status"))
                        .build(),
                instructorScope,
                actorUserId
        );
    }
}
