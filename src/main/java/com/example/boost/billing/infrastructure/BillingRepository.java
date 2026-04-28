package com.example.boost.billing.infrastructure;

import com.example.boost.domain.dto.BillingSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BillingRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<BillingSummaryResponse> findSummaries(UUID actorUserId, boolean guardianScope) {
        String sql = """
                select i.id::text as code,
                       s.full_name as name,
                       i.status as status
                from fastworks_springboot.invoices i
                join fastworks_springboot.students s on s.id = i.student_id and s.deleted_at is null
                where i.deleted_at is null
                  and (
                    ? = false
                    or exists (
                        select 1
                        from fastworks_springboot.student_guardians sg
                        join fastworks_springboot.guardians g on g.id = sg.guardian_id and g.deleted_at is null
                        join fastworks_springboot.users u on lower(u.email) = lower(g.email) and u.deleted_at is null
                        where u.id = ?
                          and sg.student_id = i.student_id
                          and sg.deleted_at is null
                    )
                  )
                order by i.created_at desc
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> BillingSummaryResponse.builder()
                        .code(rs.getString("code"))
                        .name(rs.getString("name"))
                        .status(rs.getString("status"))
                        .build(),
                guardianScope,
                actorUserId
        );
    }
}
