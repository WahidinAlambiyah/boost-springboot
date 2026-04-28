package com.example.boost.security;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ScopeGuardRepository {
    private final JdbcTemplate jdbcTemplate;

    public boolean guardianOwnsStudent(UUID userId, UUID studentId) {
        Integer matched = jdbcTemplate.queryForObject(
                """
                select count(1)
                from fastworks_springboot.student_guardians sg
                join fastworks_springboot.guardians g on g.id = sg.guardian_id and g.deleted_at is null
                join fastworks_springboot.users u on lower(u.email) = lower(g.email) and u.deleted_at is null
                where u.id = ?
                  and sg.student_id = ?
                  and sg.deleted_at is null
                """,
                Integer.class,
                userId,
                studentId
        );
        return matched != null && matched > 0;
    }

    public boolean instructorOwnsClassGroup(UUID userId, UUID classGroupId) {
        Integer matched = jdbcTemplate.queryForObject(
                """
                select count(1)
                from fastworks_springboot.class_groups cg
                where cg.id = ?
                  and cg.instructor_id = ?
                  and cg.deleted_at is null
                """,
                Integer.class,
                classGroupId,
                userId
        );
        return matched != null && matched > 0;
    }

    public boolean financeOrOpsAllowedForBilling(CurrentActor actor) {
        return actor.hasRole("FINANCE") || actor.hasRole("OPS") || actor.hasRole("ADMIN");
    }
}
