package com.example.boost.master.api;

import com.example.boost.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class MasterDataController {

    private static final Map<String, String> ACADEMY_SORT_COLUMNS = Map.of(
            "code", "code",
            "name", "name",
            "active", "is_active",
            "createdAt", "created_at",
            "updatedAt", "updated_at"
    );

    private static final Map<String, String> LOCATION_SORT_COLUMNS = Map.of(
            "code", "code",
            "name", "name",
            "city", "city",
            "active", "is_active",
            "createdAt", "created_at",
            "updatedAt", "updated_at"
    );

    private static final Map<String, String> STUDENT_SORT_COLUMNS = Map.of(
            "studentNo", "student_no",
            "fullName", "full_name",
            "nickname", "nickname",
            "status", "is_active",
            "createdAt", "created_at",
            "updatedAt", "updated_at"
    );

    private static final Map<String, String> COACH_SORT_COLUMNS = Map.of(
            "coachNo", "coach_no",
            "fullName", "full_name",
            "active", "is_active",
            "createdAt", "created_at",
            "updatedAt", "updated_at"
    );

    private final JdbcTemplate jdbcTemplate;

    public MasterDataController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/academies")
    @PreAuthorize("hasAuthority('ACADEMY_READ') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<SliceResponse<AcademyResponse>> listAcademies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String keyword = normalizeSearch(search);
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        int offset = safePage * safeSize;
        String safeSort = safeSort(sort, ACADEMY_SORT_COLUMNS, "createdAt");
        String safeDirection = safeDirection(direction, "desc");
        String orderBy = resolveOrderBy(safeSort, safeDirection, ACADEMY_SORT_COLUMNS);

        List<AcademyResponse> rows = jdbcTemplate.query("""
                select id, code, name, description, phone, email, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.academies
                 where deleted_at is null
                   and (? is null or lower(code) like ? or lower(name) like ? or lower(coalesce(description, '')) like ?)
                   and (? is null or is_active = ?)
                 order by %s
                 limit ? offset ?
                """.formatted(orderBy),
                (rs, rowNum) -> new AcademyResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getBoolean("is_active"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ),
                keyword, like(keyword), like(keyword), like(keyword), active, active, safeSize + 1, offset);

        return ApiResponse.success(200, "Data akademi berhasil dimuat", toSlice(rows, safePage, safeSize, safeSort, safeDirection));
    }

    @PostMapping("/academies")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<AcademyResponse> createAcademy(@Valid @RequestBody AcademyRequest request) {
        UUID id = jdbcTemplate.queryForObject("""
                insert into fastworks_springboot.academies (code, name, description, phone, email, is_active)
                values (?, ?, ?, ?, ?, ?)
                returning id
                """, UUID.class, request.code(), request.name(), request.description(), request.phone(), request.email(), request.active());
        return ApiResponse.success(201, "Akademi berhasil dibuat", getAcademyById(id));
    }

    @PutMapping("/academies/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<AcademyResponse> updateAcademy(@PathVariable UUID id, @Valid @RequestBody AcademyRequest request) {
        jdbcTemplate.update("""
                update fastworks_springboot.academies
                   set code = ?, name = ?, description = ?, phone = ?, email = ?, is_active = ?, updated_at = now()
                 where id = ? and deleted_at is null
                """, request.code(), request.name(), request.description(), request.phone(), request.email(), request.active(), id);
        return ApiResponse.success(200, "Akademi berhasil diperbarui", getAcademyById(id));
    }

    @DeleteMapping("/academies/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('ACADEMY_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteAcademy(@PathVariable UUID id) {
        jdbcTemplate.update("update fastworks_springboot.academies set deleted_at = now(), updated_at = now() where id = ? and deleted_at is null", id);
        return ApiResponse.success(200, "Akademi berhasil dihapus", null);
    }

    @GetMapping("/academy-locations")
    @PreAuthorize("hasAuthority('LOCATION_READ') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<SliceResponse<AcademyLocationResponse>> listAcademyLocations(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String keyword = normalizeSearch(search);
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        int offset = safePage * safeSize;
        String safeSort = safeSort(sort, LOCATION_SORT_COLUMNS, "createdAt");
        String safeDirection = safeDirection(direction, "desc");
        String orderBy = resolveOrderBy(safeSort, safeDirection, LOCATION_SORT_COLUMNS);

        List<AcademyLocationResponse> rows = jdbcTemplate.query("""
                select id, academy_id, code, name, address, city, state, postal_code, country, timezone, phone, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.academy_locations
                 where deleted_at is null
                   and (? is null or academy_id = ?)
                   and (? is null or lower(code) like ? or lower(name) like ? or lower(coalesce(city, '')) like ? or lower(coalesce(address, '')) like ?)
                   and (? is null or is_active = ?)
                 order by %s
                 limit ? offset ?
                """.formatted(orderBy),
                (rs, rowNum) -> new AcademyLocationResponse(
                        rs.getObject("id", UUID.class),
                        rs.getObject("academy_id", UUID.class),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("postal_code"),
                        rs.getString("country"),
                        rs.getString("timezone"),
                        rs.getString("phone"),
                        rs.getBoolean("is_active"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ),
                academyId, academyId,
                keyword, like(keyword), like(keyword), like(keyword), like(keyword),
                active, active,
                safeSize + 1, offset);

        return ApiResponse.success(200, "Data lokasi akademi berhasil dimuat", toSlice(rows, safePage, safeSize, safeSort, safeDirection));
    }

    @PostMapping("/academy-locations")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @PreAuthorize("hasAuthority('LOCATION_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<AcademyLocationResponse> createAcademyLocation(@Valid @RequestBody AcademyLocationRequest request) {
        UUID id = jdbcTemplate.queryForObject("""
                insert into fastworks_springboot.academy_locations (academy_id, code, name, address, city, state, postal_code, country, timezone, phone, is_active)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                returning id
                """, UUID.class,
                request.academyId(), request.code(), request.name(), request.address(), request.city(), request.state(),
                request.postalCode(), request.country(), request.timezone(), request.phone(), request.active());
        return ApiResponse.success(201, "Lokasi akademi berhasil dibuat", getAcademyLocationById(id));
    }

    @PutMapping("/academy-locations/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('LOCATION_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<AcademyLocationResponse> updateAcademyLocation(@PathVariable UUID id, @Valid @RequestBody AcademyLocationRequest request) {
        jdbcTemplate.update("""
                update fastworks_springboot.academy_locations
                   set academy_id = ?, code = ?, name = ?, address = ?, city = ?, state = ?, postal_code = ?, country = ?, timezone = ?, phone = ?, is_active = ?, updated_at = now()
                 where id = ? and deleted_at is null
                """, request.academyId(), request.code(), request.name(), request.address(), request.city(), request.state(),
                request.postalCode(), request.country(), request.timezone(), request.phone(), request.active(), id);
        return ApiResponse.success(200, "Lokasi akademi berhasil diperbarui", getAcademyLocationById(id));
    }

    @DeleteMapping("/academy-locations/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('LOCATION_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteAcademyLocation(@PathVariable UUID id) {
        jdbcTemplate.update("update fastworks_springboot.academy_locations set deleted_at = now(), updated_at = now() where id = ? and deleted_at is null", id);
        return ApiResponse.success(200, "Lokasi akademi berhasil dihapus", null);
    }

    @GetMapping("/students")
    @PreAuthorize("hasAuthority('STUDENT_READ') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<SliceResponse<StudentResponse>> listStudents(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String keyword = normalizeSearch(search);
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        int offset = safePage * safeSize;
        String safeSort = safeSort(sort, STUDENT_SORT_COLUMNS, "createdAt");
        String safeDirection = safeDirection(direction, "desc");
        String orderBy = resolveOrderBy(safeSort, safeDirection, STUDENT_SORT_COLUMNS);

        List<StudentResponse> rows = jdbcTemplate.query("""
                select id, academy_id, student_no, full_name, nickname, current_level, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.students
                 where deleted_at is null
                   and (? is null or academy_id = ?)
                   and (? is null or lower(student_no) like ? or lower(full_name) like ? or lower(coalesce(nickname, '')) like ? or lower(coalesce(current_level, '')) like ?)
                   and (? is null or is_active = ?)
                 order by %s
                 limit ? offset ?
                """.formatted(orderBy),
                (rs, rowNum) -> new StudentResponse(
                        rs.getObject("id", UUID.class),
                        rs.getObject("academy_id", UUID.class),
                        rs.getString("student_no"),
                        rs.getString("full_name"),
                        rs.getString("nickname"),
                        rs.getBoolean("is_active") ? "ACTIVE" : "INACTIVE",
                        rs.getString("current_level"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ),
                academyId, academyId,
                keyword, like(keyword), like(keyword), like(keyword), like(keyword),
                active, active,
                safeSize + 1, offset);
        return ApiResponse.success(200, "Data siswa berhasil dimuat", toSlice(rows, safePage, safeSize, safeSort, safeDirection));
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @PreAuthorize("hasAuthority('STUDENT_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        UUID id = jdbcTemplate.queryForObject("""
                insert into fastworks_springboot.students (academy_id, student_no, full_name, nickname, current_level, is_active)
                values (?, ?, ?, ?, ?, ?)
                returning id
                """, UUID.class, request.academyId(), request.studentNo(), request.fullName(), request.nickname(), request.currentLevel(), request.active());
        return ApiResponse.success(201, "Siswa berhasil dibuat", getStudentById(id));
    }

    @PutMapping("/students/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('STUDENT_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<StudentResponse> updateStudent(@PathVariable UUID id, @Valid @RequestBody StudentRequest request) {
        jdbcTemplate.update("""
                update fastworks_springboot.students
                   set academy_id = ?, student_no = ?, full_name = ?, nickname = ?, current_level = ?, is_active = ?, updated_at = now()
                 where id = ? and deleted_at is null
                """, request.academyId(), request.studentNo(), request.fullName(), request.nickname(), request.currentLevel(), request.active(), id);
        return ApiResponse.success(200, "Siswa berhasil diperbarui", getStudentById(id));
    }

    @DeleteMapping("/students/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('STUDENT_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteStudent(@PathVariable UUID id) {
        jdbcTemplate.update("update fastworks_springboot.students set deleted_at = now(), updated_at = now() where id = ? and deleted_at is null", id);
        return ApiResponse.success(200, "Siswa berhasil dihapus", null);
    }

    @GetMapping("/coaches")
    @PreAuthorize("hasAuthority('COACH_READ') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<SliceResponse<CoachProfileResponse>> listCoaches(
            @RequestParam(required = false) UUID academyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String keyword = normalizeSearch(search);
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        int offset = safePage * safeSize;
        String safeSort = safeSort(sort, COACH_SORT_COLUMNS, "createdAt");
        String safeDirection = safeDirection(direction, "desc");
        String orderBy = resolveOrderBy(safeSort, safeDirection, COACH_SORT_COLUMNS);

        List<CoachProfileResponse> rows = jdbcTemplate.query("""
                select id, academy_id, user_id, coach_no, full_name, phone, specialties, bio, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.coach_profiles
                 where deleted_at is null
                   and (? is null or academy_id = ?)
                   and (? is null or lower(coalesce(coach_no, '')) like ? or lower(coalesce(full_name, '')) like ? or lower(coalesce(phone, '')) like ? or lower(coalesce(specialties, '')) like ?)
                   and (? is null or is_active = ?)
                 order by %s
                 limit ? offset ?
                """.formatted(orderBy),
                (rs, rowNum) -> new CoachProfileResponse(
                        rs.getObject("id", UUID.class),
                        rs.getObject("academy_id", UUID.class),
                        rs.getObject("user_id", UUID.class),
                        rs.getString("coach_no"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("specialties"),
                        rs.getString("bio"),
                        "FULL_TIME",
                        "SALARY",
                        rs.getBoolean("is_active"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ),
                academyId, academyId,
                keyword, like(keyword), like(keyword), like(keyword), like(keyword),
                active, active,
                safeSize + 1, offset);
        return ApiResponse.success(200, "Data coach berhasil dimuat", toSlice(rows, safePage, safeSize, safeSort, safeDirection));
    }

    @PostMapping("/coaches")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @PreAuthorize("hasAuthority('COACH_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<CoachProfileResponse> createCoach(@Valid @RequestBody CoachProfileRequest request) {
        UUID id = jdbcTemplate.queryForObject("""
                insert into fastworks_springboot.coach_profiles (academy_id, user_id, coach_no, full_name, phone, specialties, bio, is_active)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                returning id
                """, UUID.class, request.academyId(), request.userId(), request.coachNo(), request.fullName(), request.phone(), request.specialties(), request.bio(), request.active());
        return ApiResponse.success(201, "Coach berhasil dibuat", getCoachById(id));
    }

    @PutMapping("/coaches/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('COACH_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<CoachProfileResponse> updateCoach(@PathVariable UUID id, @Valid @RequestBody CoachProfileRequest request) {
        jdbcTemplate.update("""
                update fastworks_springboot.coach_profiles
                   set academy_id = ?, user_id = ?, coach_no = ?, full_name = ?, phone = ?, specialties = ?, bio = ?, is_active = ?, updated_at = now()
                 where id = ? and deleted_at is null
                """, request.academyId(), request.userId(), request.coachNo(), request.fullName(), request.phone(), request.specialties(), request.bio(), request.active(), id);
        return ApiResponse.success(200, "Coach berhasil diperbarui", getCoachById(id));
    }

    @DeleteMapping("/coaches/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('COACH_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteCoach(@PathVariable UUID id) {
        jdbcTemplate.update("update fastworks_springboot.coach_profiles set deleted_at = now(), updated_at = now() where id = ? and deleted_at is null", id);
        return ApiResponse.success(200, "Coach berhasil dihapus", null);
    }

    @GetMapping("/user-options")
    @PreAuthorize("hasAuthority('USER_READ') or hasAuthority('COACH_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<UserOptionResponse>> listUserOptions(@RequestParam(required = false) String search, @RequestParam(defaultValue = "100") Integer size) {
        String keyword = normalizeSearch(search);
        int safeSize = safeSize(size);
        List<UserOptionResponse> rows = jdbcTemplate.query("""
                select id, username, email, full_name
                  from fastworks_springboot.users
                 where deleted_at is null and is_active = true
                   and (? is null or lower(username) like ? or lower(email) like ? or lower(coalesce(full_name, '')) like ?)
                 order by username asc
                 limit ?
                """,
                (rs, rowNum) -> new UserOptionResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("full_name")
                ), keyword, like(keyword), like(keyword), like(keyword), safeSize);
        return ApiResponse.success(200, "Opsi pengguna berhasil dimuat", rows);
    }

    private AcademyResponse getAcademyById(UUID id) {
        return jdbcTemplate.queryForObject("""
                select id, code, name, description, phone, email, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.academies
                 where id = ? and deleted_at is null
                """, (rs, rowNum) -> new AcademyResponse(
                rs.getObject("id", UUID.class), rs.getString("code"), rs.getString("name"), rs.getString("description"),
                rs.getString("phone"), rs.getString("email"), rs.getBoolean("is_active"), rs.getString("created_at"), rs.getString("updated_at")
        ), id);
    }

    private AcademyLocationResponse getAcademyLocationById(UUID id) {
        return jdbcTemplate.queryForObject("""
                select id, academy_id, code, name, address, city, state, postal_code, country, timezone, phone, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.academy_locations
                 where id = ? and deleted_at is null
                """, (rs, rowNum) -> new AcademyLocationResponse(
                rs.getObject("id", UUID.class), rs.getObject("academy_id", UUID.class), rs.getString("code"), rs.getString("name"),
                rs.getString("address"), rs.getString("city"), rs.getString("state"), rs.getString("postal_code"),
                rs.getString("country"), rs.getString("timezone"), rs.getString("phone"), rs.getBoolean("is_active"),
                rs.getString("created_at"), rs.getString("updated_at")
        ), id);
    }

    private StudentResponse getStudentById(UUID id) {
        return jdbcTemplate.queryForObject("""
                select id, academy_id, student_no, full_name, nickname, current_level, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.students
                 where id = ? and deleted_at is null
                """, (rs, rowNum) -> new StudentResponse(
                rs.getObject("id", UUID.class), rs.getObject("academy_id", UUID.class), rs.getString("student_no"),
                rs.getString("full_name"), rs.getString("nickname"), rs.getBoolean("is_active") ? "ACTIVE" : "INACTIVE",
                rs.getString("current_level"), rs.getString("created_at"), rs.getString("updated_at")
        ), id);
    }

    private CoachProfileResponse getCoachById(UUID id) {
        return jdbcTemplate.queryForObject("""
                select id, academy_id, user_id, coach_no, full_name, phone, specialties, bio, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.coach_profiles
                 where id = ? and deleted_at is null
                """, (rs, rowNum) -> new CoachProfileResponse(
                rs.getObject("id", UUID.class), rs.getObject("academy_id", UUID.class), rs.getObject("user_id", UUID.class),
                rs.getString("coach_no"), rs.getString("full_name"), rs.getString("phone"), rs.getString("specialties"), rs.getString("bio"),
                "FULL_TIME", "SALARY", rs.getBoolean("is_active"), rs.getString("created_at"), rs.getString("updated_at")
        ), id);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) return null;
        return search.trim().toLowerCase();
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
    public record AcademyResponse(UUID id, String code, String name, String description, String phone, String email, boolean isActive, String createdAt, String updatedAt) {}
    public record AcademyLocationResponse(UUID id, UUID academyId, String code, String name, String address, String city, String state, String postalCode, String country, String timezone, String phone, boolean isActive, String createdAt, String updatedAt) {}
    public record StudentResponse(UUID id, UUID academyId, String studentNo, String fullName, String nickname, String status, String currentLevel, String createdAt, String updatedAt) {}
    public record CoachProfileResponse(UUID id, UUID academyId, UUID userId, String coachNo, String fullName, String phone, String specialties, String bio, String employmentType, String payType, boolean isActive, String createdAt, String updatedAt) {}
    public record UserOptionResponse(UUID id, String username, String email, String fullName) {}

    public record AcademyRequest(@NotBlank @Size(max = 50) String code, @NotBlank @Size(max = 150) String name, @Size(max = 500) String description, @Size(max = 50) String phone, @Size(max = 150) String email, @NotNull Boolean active) {}
    public record AcademyLocationRequest(@NotNull UUID academyId, @NotBlank @Size(max = 50) String code, @NotBlank @Size(max = 150) String name, @Size(max = 500) String address, @Size(max = 100) String city, @Size(max = 100) String state, @Size(max = 20) String postalCode, @Size(max = 100) String country, @Size(max = 100) String timezone, @Size(max = 50) String phone, @NotNull Boolean active) {}
    public record StudentRequest(@NotNull UUID academyId, @NotBlank @Size(max = 50) String studentNo, @NotBlank @Size(max = 255) String fullName, @Size(max = 255) String nickname, @Size(max = 50) String currentLevel, @NotNull Boolean active) {}
    public record CoachProfileRequest(@NotNull UUID academyId, @NotNull UUID userId, @Size(max = 50) String coachNo, @NotBlank @Size(max = 255) String fullName, @Size(max = 30) String phone, @Size(max = 500) String specialties, String bio, String employmentType, String payType, @NotNull Boolean active) {}
}
