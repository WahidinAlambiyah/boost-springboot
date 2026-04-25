package com.example.boost.security;

import com.example.boost.domain.dto.AuthResponse;
import com.example.boost.domain.dto.IdNameResponse;
import com.example.boost.domain.dto.PermissionResponse;
import com.example.boost.domain.dto.RegisterResponse;
import com.example.boost.domain.dto.RoleResponse;
import com.example.boost.domain.dto.UserIdentifierResponse;
import com.example.boost.domain.dto.UserResponse;
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
                        "AUDIT_READ"
                )
        );
        TestActor userManager = buildActor(
                "user_manager",
                Set.of("USER_READ", "ROLE_READ")
        );
        TestActor userSupport = buildActor(
                "user_support",
                Set.of("AUDIT_READ", "USER_DISABLE", "USER_ENABLE", "USER_UNLOCK")
        );
        TestActor userRegular = buildActor(
                "user_regular",
                Set.of()
        );

        actors = Map.of(
                "user_admin", userAdmin,
                "user_manager", userManager,
                "user_support", userSupport,
                "user_regular", userRegular
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
                new EndpointCase("GET /api/users", "GET", "/api/users", null, 200, "user_manager", "user_regular"),
                new EndpointCase("GET /api/users/{id}", "GET", "/api/users/" + SAMPLE_ID, null, 200, "user_manager", "user_regular"),
                new EndpointCase("GET /api/users/me", "GET", "/api/users/me", null, 200, "user_manager", "user_regular"),
                new EndpointCase("POST /api/users", "POST", "/api/users", "{\"username\":\"newuser\",\"email\":\"newuser@example.com\",\"password\":\"Password123!\",\"roleCodes\":[\"USER\"]}", 201, "user_admin", "user_manager"),
                new EndpointCase("PUT /api/users/{id}", "PUT", "/api/users/" + SAMPLE_ID, "{\"username\":\"updated\",\"email\":\"updated@example.com\"}", 200, "user_admin", "user_manager"),
                new EndpointCase("PUT /api/users/{id}/roles", "PUT", "/api/users/" + SAMPLE_ID + "/roles", "{\"roleCodes\":[\"USER\"]}", 200, "user_admin", "user_manager"),
                new EndpointCase("DELETE /api/users/{id}", "DELETE", "/api/users/" + SAMPLE_ID, null, 204, "user_admin", "user_manager"),
                new EndpointCase("PUT /api/users/{id}/password", "PUT", "/api/users/" + SAMPLE_ID + "/password", "{\"newPassword\":\"NewPassword123!\"}", 204, "user_admin", "user_manager"),
                new EndpointCase("POST /api/users/{id}/disable", "POST", "/api/users/" + SAMPLE_ID + "/disable", "{\"reason\":\"security policy\"}", 204, "user_support", "user_regular"),
                new EndpointCase("POST /api/users/{id}/enable", "POST", "/api/users/" + SAMPLE_ID + "/enable", null, 204, "user_support", "user_regular"),
                new EndpointCase("POST /api/users/{id}/unlock", "POST", "/api/users/" + SAMPLE_ID + "/unlock", null, 204, "user_support", "user_regular"),

                new EndpointCase("GET /api/roles", "GET", "/api/roles", null, 200, "user_manager", "user_regular"),
                new EndpointCase("GET /api/roles/lookup", "GET", "/api/roles/lookup", null, 200, "user_manager", "user_regular"),
                new EndpointCase("GET /api/roles/{id}", "GET", "/api/roles/" + SAMPLE_ID, null, 200, "user_manager", "user_regular"),
                new EndpointCase("POST /api/roles", "POST", "/api/roles", "{\"code\":\"NEW_ROLE\",\"name\":\"New Role\",\"isActive\":true}", 201, "user_admin", "user_regular"),
                new EndpointCase("PUT /api/roles/{id}", "PUT", "/api/roles/" + SAMPLE_ID, "{\"name\":\"Updated Role\"}", 200, "user_admin", "user_regular"),
                new EndpointCase("PUT /api/roles/{id}/permissions", "PUT", "/api/roles/" + SAMPLE_ID + "/permissions", "{\"permissionCodes\":[\"USER_READ\"]}", 200, "user_admin", "user_regular"),
                new EndpointCase("DELETE /api/roles/{id}", "DELETE", "/api/roles/" + SAMPLE_ID, null, 204, "user_admin", "user_regular"),

                new EndpointCase("GET /api/permissions", "GET", "/api/permissions", null, 200, "user_admin", "user_regular"),
                new EndpointCase("GET /api/permissions/lookup", "GET", "/api/permissions/lookup", null, 200, "user_admin", "user_regular"),
                new EndpointCase("GET /api/permissions/{id}", "GET", "/api/permissions/" + SAMPLE_ID, null, 200, "user_admin", "user_regular"),
                new EndpointCase("POST /api/permissions", "POST", "/api/permissions", "{\"code\":\"NEW_PERMISSION\",\"name\":\"New Permission\",\"module\":\"USER\"}", 201, "user_admin", "user_regular"),
                new EndpointCase("PUT /api/permissions/{id}", "PUT", "/api/permissions/" + SAMPLE_ID, "{\"name\":\"Updated Permission\"}", 200, "user_admin", "user_regular"),
                new EndpointCase("DELETE /api/permissions/{id}", "DELETE", "/api/permissions/" + SAMPLE_ID, null, 204, "user_admin", "user_regular"),

                new EndpointCase("GET /api/audit-logs", "GET", "/api/audit-logs", null, 200, "user_support", "user_regular")
        );
    }

    private static Stream<EndpointCase> publicEndpoints() {
        return Stream.of(
                new EndpointCase("POST /api/auth/refresh", "POST", "/api/auth/refresh", "{\"refreshToken\":\"dummy-refresh\"}", 200, "user_regular", "user_regular"),
                new EndpointCase("POST /api/auth/logout", "POST", "/api/auth/logout", "{\"refreshToken\":\"dummy-refresh\"}", 204, "user_regular", "user_regular"),
                new EndpointCase("POST /api/auth/password-reset/request", "POST", "/api/auth/password-reset/request", "{\"email\":\"public@example.com\",\"mode\":\"OTP\"}", 204, "user_regular", "user_regular")
        );
    }

    private record EndpointCase(String description,
                                String method,
                                String path,
                                String requestBody,
                                int successStatus,
                                String authorizedActor,
                                String forbiddenActor) {

        @Override
        public String toString() {
            return description;
        }
    }

    private record TestActor(String username, String token, Set<String> authorities) {
    }
}
