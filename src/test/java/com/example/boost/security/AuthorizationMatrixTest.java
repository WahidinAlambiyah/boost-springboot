package com.example.boost.security;

import com.example.boost.domain.dto.AuthResponse;
import com.example.boost.domain.dto.IdNameResponse;
import com.example.boost.domain.dto.PaymentRequest;
import com.example.boost.domain.dto.PaymentResponse;
import com.example.boost.domain.dto.PermissionResponse;
import com.example.boost.domain.dto.RegisterResponse;
import com.example.boost.domain.dto.RoleResponse;
import com.example.boost.domain.dto.UserIdentifierResponse;
import com.example.boost.domain.dto.UserResponse;
import com.example.boost.catalog.application.AcademyLocationService;
import com.example.boost.catalog.application.CatalogService;
import com.example.boost.scheduling.application.AttendanceService;
import com.example.boost.scheduling.application.EnrollmentService;
import com.example.boost.scheduling.application.SchedulingService;
import com.example.boost.billing.application.BillingService;
import com.example.boost.notification.application.NotificationService;
import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.AuthService;
import com.example.boost.iam.application.PasswordResetService;
import com.example.boost.iam.application.PermissionService;
import com.example.boost.iam.application.RoleService;
import com.example.boost.iam.application.TokenService;
import com.example.boost.iam.application.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=fastworks_springboot",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema.sql",
        "spring.liquibase.enabled=false",
        "DB_SCHEMA=public",
        "spring.datasource.hikari.schema=public",
        "spring.datasource.hikari.connection-init-sql="
})
@AutoConfigureMockMvc
@Tag("rbac-release-gate")
@Tag("refactor-gate")
class AuthorizationMatrixTest {

    private static final UUID SAMPLE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private RoleService roleService;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private AuthService authService;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private CatalogService catalogService;

    @MockBean
    private AcademyLocationService academyLocationService;

    @MockBean
    private SchedulingService schedulingService;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private AttendanceService attendanceService;

    @MockBean
    private BillingService billingService;

    @MockBean
    private NotificationService notificationService;

    private Map<String, TestActor> actors;

    @BeforeEach
    void setUp() {
        TestActor userAdmin = buildActor(
                "user_admin",
                Set.of(
                        "ROLE_ADMIN",
                        "USER_READ", "USER_WRITE", "USER_DELETE", "USER_DISABLE", "USER_ENABLE", "USER_UNLOCK",
                        "ROLE_READ", "ROLE_WRITE", "ROLE_DELETE",
                        "PERMISSION_READ", "PERMISSION_WRITE", "PERMISSION_DELETE",
                        "AUDIT_READ",
                        "CLASS_READ", "CLASS_WRITE",
                        "SCHEDULE_READ", "SCHEDULE_WRITE",
                        "ENROLLMENT_READ", "ENROLLMENT_WRITE",
                        "ATTENDANCE_READ", "ATTENDANCE_MARK",
                        "BILLING_READ", "BILLING_WRITE",
                        "NOTIFICATION_READ", "NOTIFICATION_WRITE",
                        "LOCATION_READ", "LOCATION_WRITE"
                )
        );
        TestActor opsUser = buildActor(
                "ops_user",
                Set.of(
                        "ROLE_OPS",
                        "CLASS_READ", "CLASS_WRITE",
                        "SCHEDULE_READ", "SCHEDULE_WRITE",
                        "ENROLLMENT_READ", "ENROLLMENT_WRITE",
                        "ATTENDANCE_READ", "ATTENDANCE_MARK",
                        "NOTIFICATION_READ", "NOTIFICATION_WRITE",
                        "LOCATION_READ", "LOCATION_WRITE"
                )
        );
        TestActor instructorUser = buildActor(
                "instructor_user",
                Set.of(
                        "ROLE_INSTRUCTOR",
                        "CLASS_READ",
                        "SCHEDULE_READ",
                        "ATTENDANCE_READ", "ATTENDANCE_MARK",
                        "NOTIFICATION_READ"
                )
        );
        TestActor financeUser = buildActor(
                "finance_user",
                Set.of(
                        "ROLE_FINANCE",
                        "BILLING_READ", "BILLING_WRITE",
                        "ENROLLMENT_READ"
                )
        );
        TestActor guardianUser = buildActor(
                "guardian_user",
                Set.of(
                        "ROLE_GUARDIAN",
                        "CLASS_READ",
                        "ENROLLMENT_READ",
                        "ATTENDANCE_READ",
                        "BILLING_READ",
                        "NOTIFICATION_READ"
                )
        );

        actors = Map.of(
                "user_admin", userAdmin,
                "ops_user", opsUser,
                "instructor_user", instructorUser,
                "finance_user", financeUser,
                "guardian_user", guardianUser
        );

        when(userDetailsService.loadUserByUsername(anyString())).thenAnswer(invocation -> {
            String username = invocation.getArgument(0);
            TestActor actor = actors.get(username);
            if (actor == null) {
                throw new IllegalArgumentException("Unknown actor: " + username);
            }
            return toUserDetails(actor);
        });

        when(tokenService.isAccessTokenBlacklisted(anyString())).thenReturn(false);

        when(userService.getUsers(any(), nullable(String.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        UserResponse userResponse = new UserResponse();
        userResponse.setId(SAMPLE_ID);
        userResponse.setUsername("seed-user");
        when(userService.getById(any())).thenReturn(userResponse);
        when(userService.getCurrentUser(any())).thenReturn(userResponse);
        when(userService.createUser(any())).thenReturn(new UserIdentifierResponse("new-user@example.com"));
        when(userService.updateUser(any(), any())).thenReturn(new UserIdentifierResponse("updated-user@example.com"));
        when(userService.assignRoles(any(), any())).thenReturn(userResponse);
        doNothing().when(userService).softDeleteUser(any());
        doNothing().when(userService).changePassword(any(), anyString());
        doNothing().when(userService).disableUser(any(), anyString());
        doNothing().when(userService).enableUser(any());
        doNothing().when(userService).unlockUser(any());

        RoleResponse roleResponse = new RoleResponse();
        roleResponse.setId(SAMPLE_ID);
        roleResponse.setCode("ROLE_SAMPLE");
        when(roleService.getRoles(any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        when(roleService.getRoleLookups()).thenReturn(List.of(IdNameResponse.builder().id(SAMPLE_ID).name("ROLE_SAMPLE").build()));
        when(roleService.getById(any())).thenReturn(roleResponse);
        when(roleService.createRole(any())).thenReturn(roleResponse);
        when(roleService.updateRole(any(), any())).thenReturn(roleResponse);
        when(roleService.assignPermissions(any(), any())).thenReturn(roleResponse);
        doNothing().when(roleService).deleteRole(any());

        PermissionResponse permissionResponse = new PermissionResponse();
        permissionResponse.setId(SAMPLE_ID);
        permissionResponse.setCode("PERM_SAMPLE");
        when(permissionService.getPermissions(any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        when(permissionService.getPermissionLookups()).thenReturn(List.of(IdNameResponse.builder().id(SAMPLE_ID).name("PERM_SAMPLE").build()));
        when(permissionService.getById(any())).thenReturn(permissionResponse);
        when(permissionService.createPermission(any())).thenReturn(permissionResponse);
        when(permissionService.updatePermission(any(), any())).thenReturn(permissionResponse);
        doNothing().when(permissionService).deletePermission(any());

        when(auditLogService.findAllResponses(any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .build();
        when(authService.login(any())).thenReturn(authResponse);
        when(authService.refresh(any())).thenReturn(authResponse);
        when(authService.register(any())).thenReturn(RegisterResponse.builder()
                .username("public-user")
                .email("public-user@example.com")
                .roleCodes(Set.of("USER"))
                .build());
        doNothing().when(authService).logout(any(), any());

        doNothing().when(passwordResetService).requestReset(anyString(), any());
        doNothing().when(passwordResetService).resendOtp(any());
        doNothing().when(passwordResetService).confirmWithOtp(any(), anyString(), anyString());
        doNothing().when(passwordResetService).confirmWithToken(anyString(), anyString());

        when(catalogService.getSummaries()).thenReturn(List.of());
        when(catalogService.getWritableSummaryCount()).thenReturn(0L);

        when(academyLocationService.list(any(), nullable(String.class), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        when(schedulingService.getSummaries()).thenReturn(List.of());
        when(schedulingService.getWritableSummaryCount()).thenReturn(0L);
        doNothing().when(schedulingService).reschedule(any(), anyString(), anyString(), anyString());

        when(enrollmentService.getSummaries()).thenReturn(List.of());
        when(enrollmentService.getWritableSummaryCount()).thenReturn(0L);
        when(enrollmentService.register(any(), any(), anyString()))
                .thenReturn(new com.example.boost.domain.dto.EnrollmentActionResponse(SAMPLE_ID, "ENROLLED", null));
        when(enrollmentService.cancelAndPromote(any()))
                .thenReturn(new com.example.boost.domain.dto.EnrollmentActionResponse(SAMPLE_ID, "CANCELLED", null));

        when(attendanceService.getSummaries()).thenReturn(List.of());
        when(attendanceService.getWritableSummaryCount()).thenReturn(0L);
        doNothing().when(attendanceService).submit(any(), anyString(), anyInt(), anyInt());

        when(billingService.getSummaries()).thenReturn(List.of());
        when(billingService.getWritableSummaryCount()).thenReturn(0L);
        when(billingService.pay(any(PaymentRequest.class), anyString()))
                .thenReturn(new PaymentResponse(
                        SAMPLE_ID,
                        SAMPLE_ID,
                        java.math.BigDecimal.valueOf(100000),
                        "SUCCESS"
                ));

        when(notificationService.getSummaries()).thenReturn(List.of());
        when(notificationService.getWritableSummaryCount()).thenReturn(0L);
    }

    @DisplayName("Protected endpoint: role berhak mendapat status sukses")
    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("protectedEndpoints")
    void protectedEndpointAllowsAuthorizedRole(EndpointCase endpointCase) throws Exception {
        mockMvc.perform(buildRequest(endpointCase, tokenFor(endpointCase.authorizedActor())))
                .andExpect(status().is(endpointCase.successStatus()));
    }

    @DisplayName("Protected endpoint: role tidak berhak mendapat 403")
    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("protectedEndpoints")
    void protectedEndpointRejectsUnauthorizedRole(EndpointCase endpointCase) throws Exception {
        mockMvc.perform(buildRequest(endpointCase, tokenFor(endpointCase.forbiddenActor())))
                .andExpect(status().isForbidden());
    }

    @DisplayName("Protected endpoint: non-auth mendapat 401")
    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("protectedEndpoints")
    void protectedEndpointRejectsNonAuth(EndpointCase endpointCase) throws Exception {
        mockMvc.perform(buildRequest(endpointCase, null))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Endpoint publik tetap publik")
    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("publicEndpoints")
    void publicEndpointStaysPublic(EndpointCase endpointCase) throws Exception {
        mockMvc.perform(buildRequest(endpointCase, null))
                .andExpect(status().is(endpointCase.successStatus()));
    }

    private MockHttpServletRequestBuilder buildRequest(EndpointCase endpointCase, String token) {
        MockHttpServletRequestBuilder builder = switch (endpointCase.method()) {
            case "GET" -> get(endpointCase.path());
            case "POST" -> post(endpointCase.path());
            case "PUT" -> put(endpointCase.path());
            case "DELETE" -> delete(endpointCase.path());
            default -> throw new IllegalArgumentException("Unsupported method: " + endpointCase.method());
        };

        if (endpointCase.requestBody() != null) {
            builder.contentType(MediaType.APPLICATION_JSON)
                    .content(endpointCase.requestBody());
        }
        if (endpointCase.idempotencyKey() != null) {
            builder.header("Idempotency-Key", endpointCase.idempotencyKey());
        }
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private String tokenFor(String actorKey) {
        return actors.get(actorKey).token();
    }

    private TestActor buildActor(String username, Set<String> authorities) {
        String token = jwtService.generateAccessToken(username, UUID.randomUUID(), List.of(), List.copyOf(authorities));
        return new TestActor(username, token, authorities);
    }

    private UserDetails toUserDetails(TestActor actor) {
        List<SimpleGrantedAuthority> grantedAuthorities = actor.authorities().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return User.withUsername(actor.username())
                .password("not-used")
                .authorities(grantedAuthorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private static Stream<EndpointCase> protectedEndpoints() {
        return Stream.of(
                new EndpointCase("GET /api/users", "GET", "/api/users", null, 200, "user_admin", "ops_user"),
                new EndpointCase("GET /api/users/{id}", "GET", "/api/users/" + SAMPLE_ID, null, 200, "user_admin", "ops_user"),
                new EndpointCase("GET /api/users/me", "GET", "/api/users/me", null, 200, "user_admin", "ops_user"),
                new EndpointCase("POST /api/users", "POST", "/api/users", "{\"username\":\"newuser\",\"email\":\"newuser@example.com\",\"password\":\"Password123!\",\"roleCodes\":[\"USER\"]}", 201, "user_admin", "ops_user"),
                new EndpointCase("PUT /api/users/{id}", "PUT", "/api/users/" + SAMPLE_ID, "{\"username\":\"updated\",\"email\":\"updated@example.com\"}", 200, "user_admin", "ops_user"),
                new EndpointCase("PUT /api/users/{id}/roles", "PUT", "/api/users/" + SAMPLE_ID + "/roles", "{\"roleCodes\":[\"USER\"]}", 200, "user_admin", "ops_user"),
                new EndpointCase("DELETE /api/users/{id}", "DELETE", "/api/users/" + SAMPLE_ID, null, 204, "user_admin", "ops_user"),
                new EndpointCase("PUT /api/users/{id}/password", "PUT", "/api/users/" + SAMPLE_ID + "/password", "{\"newPassword\":\"NewPassword123!\"}", 204, "user_admin", "ops_user"),
                new EndpointCase("POST /api/users/{id}/disable", "POST", "/api/users/" + SAMPLE_ID + "/disable", "{\"reason\":\"security policy\"}", 204, "user_admin", "guardian_user"),
                new EndpointCase("POST /api/users/{id}/enable", "POST", "/api/users/" + SAMPLE_ID + "/enable", null, 204, "user_admin", "guardian_user"),
                new EndpointCase("POST /api/users/{id}/unlock", "POST", "/api/users/" + SAMPLE_ID + "/unlock", null, 204, "user_admin", "guardian_user"),

                new EndpointCase("GET /api/roles", "GET", "/api/roles", null, 200, "user_admin", "ops_user"),
                new EndpointCase("GET /api/roles/lookup", "GET", "/api/roles/lookup", null, 200, "user_admin", "ops_user"),
                new EndpointCase("GET /api/roles/{id}", "GET", "/api/roles/" + SAMPLE_ID, null, 200, "user_admin", "ops_user"),
                new EndpointCase("POST /api/roles", "POST", "/api/roles", "{\"code\":\"NEW_ROLE\",\"name\":\"New Role\",\"isActive\":true}", 201, "user_admin", "ops_user"),
                new EndpointCase("PUT /api/roles/{id}", "PUT", "/api/roles/" + SAMPLE_ID, "{\"name\":\"Updated Role\"}", 200, "user_admin", "ops_user"),
                new EndpointCase("PUT /api/roles/{id}/permissions", "PUT", "/api/roles/" + SAMPLE_ID + "/permissions", "{\"permissionCodes\":[\"USER_READ\"]}", 200, "user_admin", "ops_user"),
                new EndpointCase("DELETE /api/roles/{id}", "DELETE", "/api/roles/" + SAMPLE_ID, null, 204, "user_admin", "ops_user"),

                new EndpointCase("GET /api/permissions", "GET", "/api/permissions", null, 200, "user_admin", "ops_user"),
                new EndpointCase("GET /api/permissions/lookup", "GET", "/api/permissions/lookup", null, 200, "user_admin", "ops_user"),
                new EndpointCase("GET /api/permissions/{id}", "GET", "/api/permissions/" + SAMPLE_ID, null, 200, "user_admin", "ops_user"),
                new EndpointCase("POST /api/permissions", "POST", "/api/permissions", "{\"code\":\"NEW_PERMISSION\",\"name\":\"New Permission\",\"module\":\"USER\"}", 201, "user_admin", "ops_user"),
                new EndpointCase("PUT /api/permissions/{id}", "PUT", "/api/permissions/" + SAMPLE_ID, "{\"name\":\"Updated Permission\"}", 200, "user_admin", "ops_user"),
                new EndpointCase("DELETE /api/permissions/{id}", "DELETE", "/api/permissions/" + SAMPLE_ID, null, 204, "user_admin", "ops_user"),

                new EndpointCase("GET /api/audit-logs", "GET", "/api/audit-logs", null, 200, "user_admin", "ops_user"),
                new EndpointCase("GET /api/catalog", "GET", "/api/catalog", null, 200, "ops_user", "finance_user"),
                new EndpointCase("GET /api/catalog/summary-count", "GET", "/api/catalog/summary-count", null, 200, "ops_user", "guardian_user"),
                new EndpointCase("GET /api/academy-locations", "GET", "/api/academy-locations?academyId=" + SAMPLE_ID, null, 200, "user_admin", "guardian_user"),
                new EndpointCase("POST /api/academy-locations", "POST", "/api/academy-locations", "{\"academyId\":\"11111111-1111-1111-1111-111111111111\",\"code\":\"JKT01\",\"name\":\"Jakarta Center\",\"address\":\"Jl. Sudirman 1\",\"city\":\"Jakarta\",\"state\":\"DKI Jakarta\",\"postalCode\":\"10220\",\"country\":\"ID\",\"timezone\":\"Asia/Jakarta\"}", 201, "user_admin", "guardian_user"),
                new EndpointCase("GET /api/scheduling", "GET", "/api/scheduling", null, 200, "instructor_user", "guardian_user"),
                new EndpointCase("GET /api/scheduling/summary-count", "GET", "/api/scheduling/summary-count", null, 200, "ops_user", "guardian_user"),
                new EndpointCase("POST /api/scheduling/reschedule", "POST", "/api/scheduling/reschedule", "{\"classGroupId\":\"11111111-1111-1111-1111-111111111111\",\"previousStartAt\":\"2026-01-01T08:00:00Z\",\"newStartAt\":\"2026-01-01T10:00:00Z\",\"reason\":\"Instructor availability\"}", 202, "ops_user", "guardian_user"),
                new EndpointCase("GET /api/enrollment", "GET", "/api/enrollment", null, 200, "guardian_user", "instructor_user"),
                new EndpointCase("GET /api/enrollment/summary-count", "GET", "/api/enrollment/summary-count", null, 200, "ops_user", "guardian_user"),
                new EndpointCase("POST /api/enrollment/register", "POST", "/api/enrollment/register", "{\"studentId\":\"11111111-1111-1111-1111-111111111111\",\"classGroupId\":\"22222222-2222-2222-2222-222222222222\"}", 201, "ops_user", "guardian_user", "test-idem-key"),
                new EndpointCase("POST /api/enrollment/{id}/cancel", "POST", "/api/enrollment/" + SAMPLE_ID + "/cancel", null, 200, "ops_user", "guardian_user"),
                new EndpointCase("GET /api/attendance", "GET", "/api/attendance", null, 200, "guardian_user", "finance_user"),
                new EndpointCase("GET /api/attendance/summary-count", "GET", "/api/attendance/summary-count", null, 200, "instructor_user", "guardian_user"),
                new EndpointCase("POST /api/attendance/submit", "POST", "/api/attendance/submit", "{\"classGroupId\":\"11111111-1111-1111-1111-111111111111\",\"sessionDate\":\"2026-01-01\",\"presentCount\":10,\"absentCount\":2}", 202, "instructor_user", "guardian_user"),
                new EndpointCase("GET /api/billing", "GET", "/api/billing", null, 200, "finance_user", "instructor_user"),
                new EndpointCase("GET /api/billing/summary-count", "GET", "/api/billing/summary-count", null, 200, "finance_user", "guardian_user"),
                new EndpointCase("POST /api/billing/pay", "POST", "/api/billing/pay", "{\"invoiceId\":\"11111111-1111-1111-1111-111111111111\",\"amount\":250000}", 201, "finance_user", "guardian_user", "billing-idem-key"),
                new EndpointCase("GET /api/notification", "GET", "/api/notification", null, 200, "guardian_user", "finance_user"),
                new EndpointCase("GET /api/notification/summary-count", "GET", "/api/notification/summary-count", null, 200, "ops_user", "guardian_user")
        );
    }

    private static Stream<EndpointCase> publicEndpoints() {
        return Stream.of(
                new EndpointCase("POST /api/auth/refresh", "POST", "/api/auth/refresh", "{\"refreshToken\":\"dummy-refresh\"}", 200, "guardian_user", "guardian_user"),
                new EndpointCase("POST /api/auth/logout", "POST", "/api/auth/logout", "{\"refreshToken\":\"dummy-refresh\"}", 204, "guardian_user", "guardian_user"),
                new EndpointCase("POST /api/auth/password-reset/request", "POST", "/api/auth/password-reset/request", "{\"email\":\"public@example.com\",\"mode\":\"OTP\"}", 204, "guardian_user", "guardian_user")
        );
    }

    private record EndpointCase(String description,
                                String method,
                                String path,
                                String requestBody,
                                int successStatus,
                                String authorizedActor,
                                String forbiddenActor,
                                String idempotencyKey) {

        private EndpointCase(String description,
                             String method,
                             String path,
                             String requestBody,
                             int successStatus,
                             String authorizedActor,
                             String forbiddenActor) {
            this(description, method, path, requestBody, successStatus, authorizedActor, forbiddenActor, null);
        }

        @Override
        public String toString() {
            return description;
        }
    }

    private record TestActor(String username, String token, Set<String> authorities) {
    }
}
