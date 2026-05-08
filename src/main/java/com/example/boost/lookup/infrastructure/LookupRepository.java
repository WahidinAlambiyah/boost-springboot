package com.example.boost.lookup.infrastructure;

import com.example.boost.domain.dto.LookupOptionResponse;
import com.example.boost.security.CurrentActor;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LookupRepository {
    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 500;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<LookupOptionResponse> academies(CurrentActor actor, String search, Integer limit) {
        String sql = """
                select a.id::text as id,
                       a.code as code,
                       a.name as label,
                       a.description as description,
                       null::text as metadata_placeholder
                from fastworks_springboot.academies a
                where a.is_active = true
                  and a.deleted_at is null
                  and (:search is null or lower(a.code) like :searchLike or lower(a.name) like :searchLike)
                  and %s
                order by a.name asc
                limit :limit
                """.formatted(academyAccessPredicate("a.id"));
        return query(sql, baseParams(actor, search, limit), false);
    }

    public List<LookupOptionResponse> academyLocations(CurrentActor actor, UUID academyId, String search, Integer limit) {
        String sql = """
                select al.id::text as id,
                       al.code as code,
                       al.name as label,
                       al.address as description,
                       null::text as metadata_placeholder
                from fastworks_springboot.academy_locations al
                where al.is_active = true
                  and al.deleted_at is null
                  and (:academyId is null or al.academy_id = :academyId)
                  and (:search is null or lower(al.code) like :searchLike or lower(al.name) like :searchLike)
                  and %s
                order by al.name asc
                limit :limit
                """.formatted(academyAccessPredicate("al.academy_id"));
        return query(sql, baseParams(actor, search, limit).addValue("academyId", academyId), false);
    }

    public List<LookupOptionResponse> coaches(CurrentActor actor, UUID academyId, String search, Integer limit) {
        String sql = """
                select cp.id::text as id,
                       cp.coach_no as code,
                       coalesce(cp.full_name, u.full_name, u.username) as label,
                       cp.specialties as description,
                       cp.phone as phone,
                       cp.monthly_salary as monthly_salary
                from fastworks_springboot.coach_profiles cp
                join fastworks_springboot.users u on u.id = cp.user_id and u.deleted_at is null
                where cp.is_active = true
                  and cp.deleted_at is null
                  and (:academyId is null or cp.academy_id = :academyId)
                  and (:search is null or lower(coalesce(cp.coach_no, '')) like :searchLike or lower(coalesce(cp.full_name, u.full_name, u.username)) like :searchLike)
                  and %s
                order by coalesce(cp.full_name, u.full_name, u.username) asc
                limit :limit
                """.formatted(academyAccessPredicate("cp.academy_id"));
        return jdbcTemplate.query(sql, baseParams(actor, search, limit).addValue("academyId", academyId),
                (rs, rowNum) -> new LookupOptionResponse(
                        rs.getString("id"),
                        rs.getString("code"),
                        rs.getString("label"),
                        rs.getString("description"),
                        metadata(
                                "phone", rs.getString("phone"),
                                "payType", rs.getBigDecimal("monthly_salary") == null ? null : "MONTHLY"
                        )
                ));
    }

    public List<LookupOptionResponse> students(CurrentActor actor, UUID academyId, String status, String level, String search, Integer limit) {
        String sql = """
                select s.id::text as id,
                       s.student_no as code,
                       s.full_name as label,
                       s.email as description,
                       s.nickname as nickname,
                       s.current_level as current_level,
                       guardian.full_name as guardian_name
                from fastworks_springboot.students s
                left join lateral (
                    select g.full_name
                    from fastworks_springboot.student_guardians sg
                    join fastworks_springboot.guardians g on g.id = sg.guardian_id and g.deleted_at is null and g.is_active = true
                    where sg.student_id = s.id and sg.deleted_at is null
                    order by sg.is_primary desc, g.full_name asc
                    limit 1
                ) guardian on true
                where s.is_active = true
                  and s.deleted_at is null
                  and (:academyId is null or s.academy_id = :academyId)
                  and (:status is null or upper(:status) = 'ACTIVE')
                  and (:level is null or upper(coalesce(s.current_level, '')) = upper(:level))
                  and (:search is null or lower(coalesce(s.student_no, '')) like :searchLike or lower(s.full_name) like :searchLike or lower(coalesce(s.nickname, '')) like :searchLike)
                  and %s
                order by s.full_name asc
                limit :limit
                """.formatted(academyAccessPredicate("s.academy_id"));
        return jdbcTemplate.query(sql, baseParams(actor, search, limit)
                        .addValue("academyId", academyId)
                        .addValue("status", blankToNull(status))
                        .addValue("level", blankToNull(level)),
                (rs, rowNum) -> new LookupOptionResponse(
                        rs.getString("id"),
                        rs.getString("code"),
                        rs.getString("label"),
                        rs.getString("description"),
                        metadata(
                                "nickname", rs.getString("nickname"),
                                "currentLevel", rs.getString("current_level"),
                                "guardianName", rs.getString("guardian_name")
                        )
                ));
    }

    public List<LookupOptionResponse> classGroups(CurrentActor actor, UUID academyId, String search, Integer limit) {
        String sql = """
                select cg.id::text as id,
                       cg.code as code,
                       cg.name as label,
                       c.name as description,
                       cg.capacity as capacity,
                       cg.status as status
                from fastworks_springboot.class_groups cg
                left join fastworks_springboot.courses c on c.id = cg.course_id and c.deleted_at is null
                where cg.deleted_at is null
                  and coalesce(upper(cg.status), 'ACTIVE') not in ('INACTIVE', 'ARCHIVED')
                  and (:academyId is null or cg.academy_id = :academyId)
                  and (:search is null or lower(coalesce(cg.code, '')) like :searchLike or lower(coalesce(cg.name, '')) like :searchLike)
                  and %s
                order by cg.name asc
                limit :limit
                """.formatted(academyAccessPredicate("cg.academy_id"));
        return jdbcTemplate.query(sql, baseParams(actor, search, limit).addValue("academyId", academyId),
                (rs, rowNum) -> new LookupOptionResponse(
                        rs.getString("id"),
                        rs.getString("code"),
                        rs.getString("label"),
                        rs.getString("description"),
                        metadata("capacity", rs.getObject("capacity"), "status", rs.getString("status"))
                ));
    }

    public List<LookupOptionResponse> classSessions(CurrentActor actor, UUID academyId, LocalDate date, String status, String search, Integer limit) {
        String sql = """
                select cs.id::text as id,
                       coalesce(cs.topic, cg.code, cs.id::text) as code,
                       concat(coalesce(cg.name, cg.code, 'Class Session'), ' - ', cs.session_date, ' ', to_char(cs.start_time, 'HH24:MI')) as label,
                       cs.topic as description,
                       cs.session_date,
                       cs.start_time,
                       cs.end_time,
                       al.name as location_name,
                       coalesce(cg.name, cg.code) as class_group_name,
                       cs.status
                from fastworks_springboot.class_sessions cs
                join fastworks_springboot.class_groups cg on cg.id = cs.class_group_id and cg.deleted_at is null
                left join fastworks_springboot.academy_locations al on al.id = cs.location_id and al.deleted_at is null
                where cs.deleted_at is null
                  and coalesce(upper(cs.status), 'SCHEDULED') not in ('CANCELLED', 'ARCHIVED')
                  and (:academyId is null or cs.academy_id = :academyId)
                  and (:sessionDate is null or cs.session_date = :sessionDate)
                  and (:status is null or upper(cs.status) = upper(:status))
                  and (:search is null or lower(coalesce(cs.topic, '')) like :searchLike or lower(coalesce(cg.name, cg.code, '')) like :searchLike)
                  and %s
                order by cs.session_date desc, cs.start_time asc
                limit :limit
                """.formatted(academyAccessPredicate("cs.academy_id"));
        return jdbcTemplate.query(sql, baseParams(actor, search, limit)
                        .addValue("academyId", academyId)
                        .addValue("sessionDate", date == null ? null : Date.valueOf(date))
                        .addValue("status", blankToNull(status)),
                (rs, rowNum) -> new LookupOptionResponse(
                        rs.getString("id"),
                        rs.getString("code"),
                        rs.getString("label"),
                        rs.getString("description"),
                        metadata(
                                "sessionDate", rs.getDate("session_date").toLocalDate().toString(),
                                "startTime", rs.getTime("start_time").toLocalTime().toString(),
                                "endTime", rs.getTime("end_time").toLocalTime().toString(),
                                "locationName", rs.getString("location_name"),
                                "classGroupName", rs.getString("class_group_name"),
                                "status", rs.getString("status")
                        )
                ));
    }

    public List<LookupOptionResponse> assessmentSkills(CurrentActor actor, UUID academyId, String search, Integer limit) {
        String sql = """
                select ask.id::text as id,
                       ask.code as code,
                       ask.name as label,
                       ask.description as description,
                       ask.max_score as max_score,
                       ask.order_no as order_no
                from fastworks_springboot.assessment_skills ask
                where ask.deleted_at is null
                  and ask.is_active = true
                  and (:academyId is null or ask.academy_id = :academyId or ask.academy_id is null)
                  and (:search is null or lower(ask.code) like :searchLike or lower(ask.name) like :searchLike)
                  and (ask.academy_id is null or %s)
                order by ask.order_no nulls last, ask.name asc
                limit :limit
                """.formatted(academyAccessPredicate("ask.academy_id"));
        return jdbcTemplate.query(sql, baseParams(actor, search, limit).addValue("academyId", academyId),
                (rs, rowNum) -> new LookupOptionResponse(
                        rs.getString("id"),
                        rs.getString("code"),
                        rs.getString("label"),
                        rs.getString("description"),
                        metadata("maxScore", rs.getObject("max_score"), "orderNo", rs.getObject("order_no"))
                ));
    }

    public List<LookupOptionResponse> trainingPackages(CurrentActor actor, UUID academyId, String search, Integer limit) {
        String sql = """
                select tp.id::text as id,
                       tp.code as code,
                       tp.name as label,
                       tp.description as description,
                       tp.package_type,
                       tp.total_sessions,
                       tp.price
                from fastworks_springboot.training_packages tp
                where tp.is_active = true
                  and tp.deleted_at is null
                  and (:academyId is null or tp.academy_id = :academyId)
                  and (:search is null or lower(tp.code) like :searchLike or lower(tp.name) like :searchLike)
                  and %s
                order by tp.name asc
                limit :limit
                """.formatted(academyAccessPredicate("tp.academy_id"));
        return jdbcTemplate.query(sql, baseParams(actor, search, limit).addValue("academyId", academyId),
                (rs, rowNum) -> new LookupOptionResponse(
                        rs.getString("id"),
                        rs.getString("code"),
                        rs.getString("label"),
                        rs.getString("description"),
                        metadata(
                                "packageType", rs.getString("package_type"),
                                "totalSessions", rs.getObject("total_sessions"),
                                "price", rs.getBigDecimal("price")
                        )
                ));
    }

    public List<LookupOptionResponse> events(CurrentActor actor, UUID academyId, String search, Integer limit) {
        // Event module has not been introduced in the database yet; keep the lookup contract stable.
        return List.of();
    }

    private List<LookupOptionResponse> query(String sql, MapSqlParameterSource params, boolean withMetadata) {
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new LookupOptionResponse(
                rs.getString("id"),
                rs.getString("code"),
                rs.getString("label"),
                rs.getString("description"),
                withMetadata ? Map.of() : null
        ));
    }

    private MapSqlParameterSource baseParams(CurrentActor actor, String search, Integer limit) {
        String normalizedSearch = blankToNull(search);
        return new MapSqlParameterSource()
                .addValue("userId", actor.userId())
                .addValue("superAdmin", isSuperAdmin(actor))
                .addValue("search", normalizedSearch)
                .addValue("searchLike", normalizedSearch == null ? null : "%" + normalizedSearch.toLowerCase() + "%")
                .addValue("limit", normalizeLimit(limit));
    }

    private String academyAccessPredicate(String academyExpression) {
        return """
                (:superAdmin = true
                 or exists (
                    select 1
                    from fastworks_springboot.coach_profiles scope_cp
                    where scope_cp.academy_id = %s
                      and scope_cp.user_id = :userId
                      and scope_cp.is_active = true
                      and scope_cp.deleted_at is null
                 )
                 or exists (
                    select 1
                    from fastworks_springboot.guardians scope_g
                    join fastworks_springboot.users scope_u on lower(scope_u.email) = lower(scope_g.email) and scope_u.deleted_at is null
                    where scope_g.academy_id = %s
                      and scope_u.id = :userId
                      and scope_g.is_active = true
                      and scope_g.deleted_at is null
                 )
                 or exists (
                    select 1
                    from fastworks_springboot.class_groups scope_cg
                    where scope_cg.academy_id = %s
                      and scope_cg.instructor_id = :userId
                      and scope_cg.deleted_at is null
                 ))
                """.formatted(academyExpression, academyExpression, academyExpression);
    }

    private boolean isSuperAdmin(CurrentActor actor) {
        return actor.hasRole("ADMIN") || actor.hasRole("SUPER_ADMIN");
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Map<String, Object> metadata(Object... keyValues) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            Object value = keyValues[i + 1];
            if (value != null) {
                metadata.put(String.valueOf(keyValues[i]), value);
            }
        }
        return metadata.isEmpty() ? null : metadata;
    }
}
