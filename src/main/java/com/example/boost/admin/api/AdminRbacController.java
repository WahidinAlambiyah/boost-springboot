package com.example.boost.admin.api;

import com.example.boost.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rbac")
public class AdminRbacController {

    private static final Map<String, String> USER_SORT_COLUMNS = Map.of(
            "username", "u.username",
            "email", "u.email",
            "fullName", "u.full_name",
            "active", "u.is_active",
            "createdAt", "u.created_at",
            "updatedAt", "u.updated_at"
    );

    private static final Map<String, String> ROLE_SORT_COLUMNS = Map.of(
            "code", "r.code",
            "name", "r.name",
            "active", "r.is_active",
            "createdAt", "r.created_at",
            "updatedAt", "r.updated_at"
    );

    private static final Map<String, String> PERMISSION_SORT_COLUMNS = Map.of(
            "code", "code",
            "name", "name",
            "module", "module nulls last",
            "active", "is_active",
            "createdAt", "created_at",
            "updatedAt", "updated_at"
    );

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public AdminRbacController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('USER_READ') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<SliceResponse<AdminUserResponse>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String roleCode,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String keyword = normalizeSearch(search);
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        int offset = safePage * safeSize;
        String safeSort = safeSort(sort, USER_SORT_COLUMNS, "createdAt");
        String safeDirection = safeDirection(direction, "desc");
        String orderBy = resolveOrderBy(safeSort, safeDirection, USER_SORT_COLUMNS);

        List<AdminUserResponse> rows = jdbcTemplate.query("""
                select
                    u.id,
                    u.username,
                    u.email,
                    u.full_name,
                    u.is_active,
                    coalesce(array_remove(array_agg(distinct r.code order by r.code), null), '{}') as roles,
                    u.created_at::text,
                    u.updated_at::text
                from fastworks_springboot.users u
                left join fastworks_springboot.user_roles ur on ur.user_id = u.id
                left join fastworks_springboot.roles r on r.id = ur.role_id
                where u.deleted_at is null
                  and (? is null or lower(u.username) like ? or lower(u.email) like ? or lower(coalesce(u.full_name, '')) like ?)
                  and (? is null or u.is_active = ?)
                  and (? is null or exists (
                    select 1
                      from fastworks_springboot.user_roles fur
                      join fastworks_springboot.roles fr on fr.id = fur.role_id
                     where fur.user_id = u.id and fr.code = ?
                  ))
                group by u.id, u.username, u.email, u.full_name, u.is_active, u.created_at, u.updated_at
                order by %s
                limit ? offset ?
                """.formatted(orderBy),
                (rs, rowNum) -> new AdminUserResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getBoolean("is_active"),
                        List.of((String[]) rs.getArray("roles").getArray()),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ),
                keyword, like(keyword), like(keyword), like(keyword),
                active, active,
                blankToNull(roleCode), blankToNull(roleCode),
                safeSize + 1, offset);

        return ApiResponse.success(200, "Users loaded", toSlice(rows, safePage, safeSize, safeSort, safeDirection));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @PreAuthorize("hasAuthority('USER_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<AdminUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UUID userId = jdbcTemplate.queryForObject("""
                insert into fastworks_springboot.users (username, email, password_hash, full_name, is_active)
                values (?, ?, ?, ?, ?)
                returning id
                """, UUID.class,
                request.username(), request.email(), passwordEncoder.encode(request.password()), request.fullName(), request.active());
        replaceUserRoles(userId, request.roleCodes());
        return ApiResponse.success(201, "User created", getUserById(userId));
    }

    @PutMapping("/users/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('USER_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<AdminUserResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        jdbcTemplate.update("""
                update fastworks_springboot.users
                   set username = ?, email = ?, full_name = ?, is_active = ?, updated_at = now()
                 where id = ? and deleted_at is null
                """, request.username(), request.email(), request.fullName(), request.active(), id);
        replaceUserRoles(id, request.roleCodes());
        return ApiResponse.success(200, "User updated", getUserById(id));
    }

    @PatchMapping("/users/{id}/password")
    @Transactional
    @PreAuthorize("hasAuthority('USER_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<AdminUserResponse> updateUserPassword(@PathVariable UUID id, @Valid @RequestBody UpdatePasswordRequest request) {
        jdbcTemplate.update("""
                update fastworks_springboot.users
                   set password_hash = ?, updated_at = now()
                 where id = ? and deleted_at is null
                """, passwordEncoder.encode(request.password()), id);
        return ApiResponse.success(200, "Password updated", getUserById(id));
    }

    @PatchMapping("/users/{id}/active")
    @Transactional
    @PreAuthorize("hasAuthority('USER_DISABLE') or hasAuthority('USER_ENABLE') or hasAuthority('USER_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<AdminUserResponse> updateUserActive(@PathVariable UUID id, @Valid @RequestBody UpdateActiveRequest request) {
        jdbcTemplate.update("""
                update fastworks_springboot.users
                   set is_active = ?, updated_at = now()
                 where id = ? and deleted_at is null
                """, request.active(), id);
        return ApiResponse.success(200, request.active() ? "User enabled" : "User disabled", getUserById(id));
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('USER_DELETE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable UUID id) {
        jdbcTemplate.update("update fastworks_springboot.users set deleted_at = now(), updated_at = now() where id = ? and deleted_at is null", id);
        return ApiResponse.success(200, "User deleted", null);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_READ') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<SliceResponse<RoleResponse>> listRoles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String permissionCode,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "code") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        String keyword = normalizeSearch(search);
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        int offset = safePage * safeSize;
        String safeSort = safeSort(sort, ROLE_SORT_COLUMNS, "code");
        String safeDirection = safeDirection(direction, "asc");
        String orderBy = resolveOrderBy(safeSort, safeDirection, ROLE_SORT_COLUMNS);

        List<RoleResponse> rows = jdbcTemplate.query("""
                select
                    r.id,
                    r.code,
                    r.name,
                    r.description,
                    r.is_active,
                    coalesce(array_remove(array_agg(distinct p.code order by p.code), null), '{}') as permissions,
                    r.created_at::text,
                    r.updated_at::text
                from fastworks_springboot.roles r
                left join fastworks_springboot.role_permissions rp on rp.role_id = r.id
                left join fastworks_springboot.permissions p on p.id = rp.permission_id
                where (? is null or lower(r.code) like ? or lower(r.name) like ? or lower(coalesce(r.description, '')) like ?)
                  and (? is null or r.is_active = ?)
                  and (? is null or exists (
                    select 1
                      from fastworks_springboot.role_permissions frp
                      join fastworks_springboot.permissions fp on fp.id = frp.permission_id
                     where frp.role_id = r.id and fp.code = ?
                  ))
                group by r.id, r.code, r.name, r.description, r.is_active, r.created_at, r.updated_at
                order by %s
                limit ? offset ?
                """.formatted(orderBy),
                (rs, rowNum) -> new RoleResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("is_active"),
                        List.of((String[]) rs.getArray("permissions").getArray()),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ),
                keyword, like(keyword), like(keyword), like(keyword),
                active, active,
                blankToNull(permissionCode), blankToNull(permissionCode),
                safeSize + 1, offset);

        return ApiResponse.success(200, "Roles loaded", toSlice(rows, safePage, safeSize, safeSort, safeDirection));
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        UUID roleId = jdbcTemplate.queryForObject("""
                insert into fastworks_springboot.roles (code, name, description, is_active)
                values (?, ?, ?, ?)
                returning id
                """, UUID.class, request.code(), request.name(), request.description(), request.active());
        replaceRolePermissions(roleId, request.permissionCodes());
        return ApiResponse.success(201, "Role created", getRoleById(roleId));
    }

    @PutMapping("/roles/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<RoleResponse> updateRole(@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
        jdbcTemplate.update("""
                update fastworks_springboot.roles
                   set code = ?, name = ?, description = ?, is_active = ?, updated_at = now()
                 where id = ?
                """, request.code(), request.name(), request.description(), request.active(), id);
        replaceRolePermissions(id, request.permissionCodes());
        return ApiResponse.success(200, "Role updated", getRoleById(id));
    }

    @DeleteMapping("/roles/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_DELETE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteRole(@PathVariable UUID id) {
        jdbcTemplate.update("delete from fastworks_springboot.roles where id = ?", id);
        return ApiResponse.success(200, "Role deleted", null);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_READ') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<SliceResponse<PermissionResponse>> listPermissions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "module") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        String keyword = normalizeSearch(search);
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        int offset = safePage * safeSize;
        String safeSort = safeSort(sort, PERMISSION_SORT_COLUMNS, "module");
        String safeDirection = safeDirection(direction, "asc");
        String orderBy = resolveOrderBy(safeSort, safeDirection, PERMISSION_SORT_COLUMNS);
        String normalizedModule = normalizeSearch(module);

        List<PermissionResponse> rows = jdbcTemplate.query("""
                select id, code, name, description, module, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.permissions
                 where (? is null or lower(code) like ? or lower(name) like ? or lower(coalesce(module, '')) like ?)
                   and (? is null or is_active = ?)
                   and (? is null or lower(coalesce(module, '')) = ?)
                 order by %s
                 limit ? offset ?
                """.formatted(orderBy),
                (rs, rowNum) -> new PermissionResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("module"),
                        rs.getBoolean("is_active"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ),
                keyword, like(keyword), like(keyword), like(keyword),
                active, active,
                normalizedModule, normalizedModule,
                safeSize + 1, offset);
        return ApiResponse.success(200, "Permissions loaded", toSlice(rows, safePage, safeSize, safeSort, safeDirection));
    }

    @PostMapping("/permissions")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @PreAuthorize("hasAuthority('PERMISSION_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest request) {
        UUID id = jdbcTemplate.queryForObject("""
                insert into fastworks_springboot.permissions (code, name, description, module, is_active)
                values (?, ?, ?, ?, ?)
                returning id
                """, UUID.class, request.code(), request.name(), request.description(), request.module(), request.active());
        return ApiResponse.success(201, "Permission created", getPermissionById(id));
    }

    @PutMapping("/permissions/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('PERMISSION_WRITE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<PermissionResponse> updatePermission(@PathVariable UUID id, @Valid @RequestBody PermissionRequest request) {
        jdbcTemplate.update("""
                update fastworks_springboot.permissions
                   set code = ?, name = ?, description = ?, module = ?, is_active = ?, updated_at = now()
                 where id = ?
                """, request.code(), request.name(), request.description(), request.module(), request.active(), id);
        return ApiResponse.success(200, "Permission updated", getPermissionById(id));
    }

    @DeleteMapping("/permissions/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('PERMISSION_DELETE') or hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deletePermission(@PathVariable UUID id) {
        jdbcTemplate.update("delete from fastworks_springboot.permissions where id = ?", id);
        return ApiResponse.success(200, "Permission deleted", null);
    }

    private AdminUserResponse getUserById(UUID id) {
        return jdbcTemplate.queryForObject("""
                select
                    u.id,
                    u.username,
                    u.email,
                    u.full_name,
                    u.is_active,
                    coalesce(array_remove(array_agg(distinct r.code order by r.code), null), '{}') as roles,
                    u.created_at::text,
                    u.updated_at::text
                from fastworks_springboot.users u
                left join fastworks_springboot.user_roles ur on ur.user_id = u.id
                left join fastworks_springboot.roles r on r.id = ur.role_id
                where u.id = ? and u.deleted_at is null
                group by u.id, u.username, u.email, u.full_name, u.is_active, u.created_at, u.updated_at
                """,
                (rs, rowNum) -> new AdminUserResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getBoolean("is_active"),
                        List.of((String[]) rs.getArray("roles").getArray()),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ), id);
    }

    private RoleResponse getRoleById(UUID id) {
        return jdbcTemplate.queryForObject("""
                select
                    r.id,
                    r.code,
                    r.name,
                    r.description,
                    r.is_active,
                    coalesce(array_remove(array_agg(distinct p.code order by p.code), null), '{}') as permissions,
                    r.created_at::text,
                    r.updated_at::text
                from fastworks_springboot.roles r
                left join fastworks_springboot.role_permissions rp on rp.role_id = r.id
                left join fastworks_springboot.permissions p on p.id = rp.permission_id
                where r.id = ?
                group by r.id, r.code, r.name, r.description, r.is_active, r.created_at, r.updated_at
                """,
                (rs, rowNum) -> new RoleResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("is_active"),
                        List.of((String[]) rs.getArray("permissions").getArray()),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ), id);
    }

    private PermissionResponse getPermissionById(UUID id) {
        return jdbcTemplate.queryForObject("""
                select id, code, name, description, module, is_active, created_at::text, updated_at::text
                  from fastworks_springboot.permissions
                 where id = ?
                """,
                (rs, rowNum) -> new PermissionResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("module"),
                        rs.getBoolean("is_active"),
                        rs.getString("created_at"),
                        rs.getString("updated_at")
                ), id);
    }

    private void replaceUserRoles(UUID userId, List<String> roleCodes) {
        jdbcTemplate.update("delete from fastworks_springboot.user_roles where user_id = ?", userId);
        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }
        roleCodes.stream().distinct().forEach(roleCode -> jdbcTemplate.update("""
                insert into fastworks_springboot.user_roles (user_id, role_id)
                select ?, id from fastworks_springboot.roles where code = ?
                on conflict do nothing
                """, userId, roleCode));
    }

    private void replaceRolePermissions(UUID roleId, List<String> permissionCodes) {
        jdbcTemplate.update("delete from fastworks_springboot.role_permissions where role_id = ?", roleId);
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return;
        }
        permissionCodes.stream().distinct().forEach(permissionCode -> jdbcTemplate.update("""
                insert into fastworks_springboot.role_permissions (role_id, permission_id)
                select ?, id from fastworks_springboot.permissions where code = ?
                on conflict do nothing
                """, roleId, permissionCode));
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim().toLowerCase();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String like(String value) {
        if (value == null) {
            return null;
        }
        return "%" + value + "%";
    }

    private int safePage(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }

    private int safeSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private String safeSort(String sort, Map<String, String> sortColumns, String defaultSort) {
        if (sort == null || !sortColumns.containsKey(sort)) {
            return defaultSort;
        }
        return sort;
    }

    private String safeDirection(String direction, String defaultDirection) {
        if ("asc".equalsIgnoreCase(direction)) {
            return "asc";
        }
        if ("desc".equalsIgnoreCase(direction)) {
            return "desc";
        }
        return defaultDirection;
    }

    private String resolveOrderBy(String sort, String direction, Map<String, String> sortColumns) {
        String column = sortColumns.get(sort);
        if (column == null) {
            throw new IllegalArgumentException("Unsupported sort field: " + sort);
        }
        return column + " " + direction;
    }

    private <T> SliceResponse<T> toSlice(List<T> rows, int page, int size, String sort, String direction) {
        boolean hasNext = rows.size() > size;
        List<T> items = hasNext ? rows.subList(0, size) : rows;
        return new SliceResponse<>(items, page, size, hasNext, page > 0, sort, direction);
    }

    public record SliceResponse<T>(List<T> items, int page, int size, boolean hasNext, boolean hasPrevious, String sort, String direction) {}
    public record AdminUserResponse(UUID id, String username, String email, String fullName, boolean active, List<String> roles, String createdAt, String updatedAt) {}
    public record RoleResponse(UUID id, String code, String name, String description, boolean active, List<String> permissions, String createdAt, String updatedAt) {}
    public record PermissionResponse(UUID id, String code, String name, String description, String module, boolean active, String createdAt, String updatedAt) {}

    public record CreateUserRequest(@NotBlank @Size(max = 50) String username, @NotBlank @Email @Size(max = 255) String email, @NotBlank @Size(min = 8, max = 100) String password, @Size(max = 255) String fullName, @NotNull Boolean active, List<String> roleCodes) {}
    public record UpdateUserRequest(@NotBlank @Size(max = 50) String username, @NotBlank @Email @Size(max = 255) String email, @Size(max = 255) String fullName, @NotNull Boolean active, List<String> roleCodes) {}
    public record UpdatePasswordRequest(@NotBlank @Size(min = 8, max = 100) String password) {}
    public record UpdateActiveRequest(@NotNull Boolean active) {}
    public record RoleRequest(@NotBlank @Size(max = 100) String code, @NotBlank @Size(max = 150) String name, @Size(max = 500) String description, @NotNull Boolean active, List<String> permissionCodes) {}
    public record PermissionRequest(@NotBlank @Size(max = 150) String code, @NotBlank @Size(max = 150) String name, @Size(max = 500) String description, @Size(max = 100) String module, @NotNull Boolean active) {}
}
