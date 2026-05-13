package com.example.boost.master.api;

import com.example.boost.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/master")
public class MasterPackageSkillController {

    private static final Map<String, String> PACKAGE_SORT_COLUMNS = Map.of(
            "code", "code",
            "name", "name",
            "packageType", "package_type",
            "price", "price",
            "active", "is_active",
            "createdAt", "created_at",
            "updatedAt", "updated_at"
    );

    private static final Map<String, String> SKILL_SORT_COLUMNS = Map.of(
            "code", "code",
            "name", "name",
            "orderNo", "order_no",
            "maxScore", "max_score",
            "active", "is_active",
            "createdAt", "created_at",
            "updatedAt", "updated_at"
    );

    private final JdbcTemplate jdbcTemplate;

    public MasterPackageSkillController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/training-packages")
    @PreAuthorize("hasAuthority('PACKAGE_READ') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<SliceResponse<TrainingPackageResponse>> listTrainingPackages(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String packageType,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String keyword = normalizeSearch(search);
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        int offset = safePage * safeSize;
        String safeSort = safeSort(sort, PACKAGE_SORT_COLUMNS, "createdAt");
        String safeDirection = safeDirection(direction, "desc");
        String orderBy = resolveOrderBy(safeSort, safeDirection, PACKAGE_SORT_COLUMNS);
        String safePackageType = blankToNull(packageType);

        List<TrainingPackageResponse> rows = jdbcTemplate.query("""
                select id, academy_id, code, name, package_type, price, total_sessions, validity_days, description, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.training_packages
                 where deleted_at is null
                   and (? is null or academy_id = ?)
                   and (? is null or lower(code) like ? or lower(name) like ? or lower(coalesce(description, '')) like ?)
                   and (? is null or is_active = ?)
                   and (? is null or package_type = ?)
                 order by %s
                 limit ? offset ?
                """.formatted(orderBy),
                (rs, rowNum) -> new TrainingPackageResponse(
                        rs.getObject("id", UUID.class),
                        rs.getObject("academy_id", UUID.class),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("package_type"),
                        rs.getBigDecimal("price"),
                        rs.getObject("total_sessions", Integer.class),
                        rs.getObject("validity_days", Integer.class),
                        rs.getString("description"),
                        rs.getBoolean("is_active"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ),
                academyId, academyId,
                keyword, like(keyword), like(keyword), like(keyword),
                active, active,
                safePackageType, safePackageType,
                safeSize + 1, offset);
        return ApiResponse.success(200, "Data paket latihan berhasil dimuat", toSlice(rows, safePage, safeSize, safeSort, safeDirection));
    }

    @PostMapping("/training-packages")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @PreAuthorize("hasAuthority('PACKAGE_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<TrainingPackageResponse> createTrainingPackage(@Valid @RequestBody TrainingPackageRequest request) {
        UUID id = jdbcTemplate.queryForObject("""
                insert into fastworks_springboot.training_packages (academy_id, code, name, package_type, total_sessions, price, validity_days, description, is_active)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                returning id
                """, UUID.class, request.academyId(), request.code(), request.name(), request.packageType(), request.sessionQuota(), request.price(), request.validityDays(), request.description(), request.active());
        return ApiResponse.success(201, "Paket latihan berhasil dibuat", getTrainingPackageById(id));
    }

    @PutMapping("/training-packages/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('PACKAGE_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<TrainingPackageResponse> updateTrainingPackage(@PathVariable UUID id, @Valid @RequestBody TrainingPackageRequest request) {
        jdbcTemplate.update("""
                update fastworks_springboot.training_packages
                   set academy_id = ?, code = ?, name = ?, package_type = ?, total_sessions = ?, price = ?, validity_days = ?, description = ?, is_active = ?, updated_at = now()
                 where id = ? and deleted_at is null
                """, request.academyId(), request.code(), request.name(), request.packageType(), request.sessionQuota(), request.price(), request.validityDays(), request.description(), request.active(), id);
        return ApiResponse.success(200, "Paket latihan berhasil diperbarui", getTrainingPackageById(id));
    }

    @DeleteMapping("/training-packages/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('PACKAGE_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteTrainingPackage(@PathVariable UUID id) {
        jdbcTemplate.update("update fastworks_springboot.training_packages set deleted_at = now(), updated_at = now() where id = ? and deleted_at is null", id);
        return ApiResponse.success(200, "Paket latihan berhasil dihapus", null);
    }

    @GetMapping("/assessment-skills")
    @PreAuthorize("hasAuthority('ASSESSMENT_READ') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<SliceResponse<AssessmentSkillResponse>> listAssessmentSkills(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "orderNo") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        String keyword = normalizeSearch(search);
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        int offset = safePage * safeSize;
        String safeSort = safeSort(sort, SKILL_SORT_COLUMNS, "orderNo");
        String safeDirection = safeDirection(direction, "asc");
        String orderBy = resolveOrderBy(safeSort, safeDirection, SKILL_SORT_COLUMNS);

        List<AssessmentSkillResponse> rows = jdbcTemplate.query("""
                select id, academy_id, code, name, description, order_no, is_active, max_score, created_at::text, updated_at::text
                  from fastworks_springboot.assessment_skills
                 where deleted_at is null
                   and (? is null or academy_id = ? or academy_id is null)
                   and (? is null or lower(code) like ? or lower(name) like ? or lower(coalesce(description, '')) like ?)
                   and (? is null or is_active = ?)
                 order by %s
                 limit ? offset ?
                """.formatted(orderBy),
                (rs, rowNum) -> new AssessmentSkillResponse(
                        rs.getObject("id", UUID.class),
                        rs.getObject("academy_id", UUID.class),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getObject("order_no", Integer.class),
                        rs.getBoolean("is_active"),
                        rs.getInt("max_score"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ),
                academyId, academyId,
                keyword, like(keyword), like(keyword), like(keyword),
                active, active,
                safeSize + 1, offset);
        return ApiResponse.success(200, "Data skill penilaian berhasil dimuat", toSlice(rows, safePage, safeSize, safeSort, safeDirection));
    }

    @PostMapping("/assessment-skills")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<AssessmentSkillResponse> createAssessmentSkill(@Valid @RequestBody AssessmentSkillRequest request) {
        UUID id = jdbcTemplate.queryForObject("""
                insert into fastworks_springboot.assessment_skills (academy_id, code, name, description, order_no, is_active, max_score)
                values (?, ?, ?, ?, ?, ?, ?)
                returning id
                """, UUID.class, request.academyId(), request.code(), request.name(), request.description(), request.orderNo(), request.active(), request.maxScore());
        return ApiResponse.success(201, "Skill penilaian berhasil dibuat", getAssessmentSkillById(id));
    }

    @PutMapping("/assessment-skills/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<AssessmentSkillResponse> updateAssessmentSkill(@PathVariable UUID id, @Valid @RequestBody AssessmentSkillRequest request) {
        jdbcTemplate.update("""
                update fastworks_springboot.assessment_skills
                   set academy_id = ?, code = ?, name = ?, description = ?, order_no = ?, is_active = ?, max_score = ?, updated_at = now()
                 where id = ? and deleted_at is null
                """, request.academyId(), request.code(), request.name(), request.description(), request.orderNo(), request.active(), request.maxScore(), id);
        return ApiResponse.success(200, "Skill penilaian berhasil diperbarui", getAssessmentSkillById(id));
    }

    @DeleteMapping("/assessment-skills/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('ASSESSMENT_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteAssessmentSkill(@PathVariable UUID id) {
        jdbcTemplate.update("update fastworks_springboot.assessment_skills set deleted_at = now(), updated_at = now() where id = ? and deleted_at is null", id);
        return ApiResponse.success(200, "Skill penilaian berhasil dihapus", null);
    }

    private TrainingPackageResponse getTrainingPackageById(UUID id) {
        return jdbcTemplate.queryForObject("""
                select id, academy_id, code, name, package_type, price, total_sessions, validity_days, description, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.training_packages
                 where id = ? and deleted_at is null
                """, (rs, rowNum) -> new TrainingPackageResponse(
                        rs.getObject("id", UUID.class), rs.getObject("academy_id", UUID.class), rs.getString("code"), rs.getString("name"), rs.getString("package_type"), rs.getBigDecimal("price"), rs.getObject("total_sessions", Integer.class), rs.getObject("validity_days", Integer.class), rs.getString("description"), rs.getBoolean("is_active"), rs.getString("created_at"), rs.getString("updated_at")
                ), id);
    }

    private AssessmentSkillResponse getAssessmentSkillById(UUID id) {
        return jdbcTemplate.queryForObject("""
                select id, academy_id, code, name, description, order_no, is_active, max_score, created_at::text, updated_at::text
                  from fastworks_springboot.assessment_skills
                 where id = ? and deleted_at is null
                """, (rs, rowNum) -> new AssessmentSkillResponse(
                        rs.getObject("id", UUID.class), rs.getObject("academy_id", UUID.class), rs.getString("code"), rs.getString("name"), rs.getString("description"), rs.getObject("order_no", Integer.class), rs.getBoolean("is_active"), rs.getInt("max_score"), rs.getString("created_at"), rs.getString("updated_at")
                ), id);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) return null;
        return search.trim().toLowerCase();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private String like(String value) {
        if (value == null) return null;
        return "%" + value + "%";
    }

    private int safePage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    private int safeSize(Integer size) {
        if (size == null || size < 1) return 10;
        return Math.min(size, 100);
    }

    private String safeSort(String sort, Map<String, String> sortColumns, String defaultSort) {
        return sort != null && sortColumns.containsKey(sort) ? sort : defaultSort;
    }

    private String safeDirection(String direction, String defaultDirection) {
        if ("asc".equalsIgnoreCase(direction)) return "asc";
        if ("desc".equalsIgnoreCase(direction)) return "desc";
        return defaultDirection;
    }

    private String resolveOrderBy(String sort, String direction, Map<String, String> sortColumns) {
        String column = sortColumns.get(sort);
        if (column == null) throw new IllegalArgumentException("Unsupported sort field: " + sort);
        return column + " " + direction + " nulls last";
    }

    private <T> SliceResponse<T> toSlice(List<T> rows, int page, int size, String sort, String direction) {
        boolean hasNext = rows.size() > size;
        List<T> items = hasNext ? rows.subList(0, size) : rows;
        return new SliceResponse<>(items, page, size, hasNext, page > 0, sort, direction);
    }

    public record SliceResponse<T>(List<T> items, int page, int size, boolean hasNext, boolean hasPrevious, String sort, String direction) {}
    public record TrainingPackageResponse(UUID id, UUID academyId, String code, String name, String packageType, BigDecimal price, Integer sessionQuota, Integer validityDays, String description, boolean isActive, String createdAt, String updatedAt) {}
    public record AssessmentSkillResponse(UUID id, UUID academyId, String code, String name, String description, Integer orderNo, boolean active, Integer maxScore, String createdAt, String updatedAt) {}
    public record TrainingPackageRequest(@NotNull UUID academyId, @NotBlank @Size(max = 50) String code, @NotBlank @Size(max = 255) String name, @NotBlank @Size(max = 50) String packageType, @NotNull BigDecimal price, @NotNull Integer sessionQuota, Integer validityDays, String description, @NotNull Boolean active) {}
    public record AssessmentSkillRequest(UUID academyId, @NotBlank @Size(max = 50) String code, @NotBlank @Size(max = 150) String name, String description, Integer orderNo, @NotNull Boolean active, @NotNull Integer maxScore) {}
}
