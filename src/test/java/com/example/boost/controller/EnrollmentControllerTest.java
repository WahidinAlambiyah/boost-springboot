package com.example.boost.controller;

import com.example.boost.domain.dto.EnrollmentActionResponse;
import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.TokenService;
import com.example.boost.scheduling.api.EnrollmentController;
import com.example.boost.scheduling.application.EnrollmentService;
import com.example.boost.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
@Tag("refactor-gate")
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser(authorities = "ENROLLMENT_READ")
    void getSummariesSuccess() throws Exception {
        when(enrollmentService.getSummaries()).thenReturn(List.of());

        mockMvc.perform(get("/api/enrollment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Enrollments retrieved"));
    }

    @Test
    @WithMockUser(authorities = "ENROLLMENT_WRITE")
    void registerSuccess() throws Exception {
        UUID enrollmentId = UUID.randomUUID();
        when(enrollmentService.register(any(UUID.class), any(UUID.class), anyString()))
                .thenReturn(new EnrollmentActionResponse(enrollmentId, "ENROLLED", null));

        mockMvc.perform(post("/api/enrollment/register")
                        .header("Idempotency-Key", " req-key ")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "studentId", UUID.randomUUID(),
                                "classGroupId", UUID.randomUUID()
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.enrollmentId").value(enrollmentId.toString()))
                .andExpect(jsonPath("$.data.status").value("ENROLLED"));
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("endpointSecurityCases")
    void domainEndpointsMustCoverUnauthorizedForbiddenAndSuccess(EndpointAccessCase testCase) throws Exception {
        when(enrollmentService.getSummaries()).thenReturn(List.of());
        when(enrollmentService.getWritableSummaryCount()).thenReturn(0L);
        when(enrollmentService.register(any(UUID.class), any(UUID.class), anyString()))
                .thenReturn(new EnrollmentActionResponse(UUID.randomUUID(), "ENROLLED", null));
        when(enrollmentService.cancelAndPromote(any(UUID.class)))
                .thenReturn(new EnrollmentActionResponse(UUID.randomUUID(), "CANCELLED", null));

        MockHttpServletRequestBuilder request = testCase.request();
        setSecurityContext(testCase.authority());

        mockMvc.perform(request)
                .andExpect(status().is(testCase.expectedStatus()));
        SecurityContextHolder.clearContext();
    }

    private static Stream<EndpointAccessCase> endpointSecurityCases() {
        String registerBody = "{\"studentId\":\"11111111-1111-1111-1111-111111111111\",\"classGroupId\":\"22222222-2222-2222-2222-222222222222\"}";
        String enrollmentId = "33333333-3333-3333-3333-333333333333";
        return Stream.of(
                new EndpointAccessCase("GET /api/enrollment without token -> 401", get("/api/enrollment"), null, 401),
                new EndpointAccessCase("GET /api/enrollment wrong role -> 403", get("/api/enrollment"), "ENROLLMENT_WRITE", 403),
                new EndpointAccessCase("GET /api/enrollment correct role -> 200", get("/api/enrollment"), "ENROLLMENT_READ", 200),

                new EndpointAccessCase("GET /api/enrollment/summary-count without token -> 401", get("/api/enrollment/summary-count"), null, 401),
                new EndpointAccessCase("GET /api/enrollment/summary-count wrong role -> 403", get("/api/enrollment/summary-count"), "ENROLLMENT_READ", 403),
                new EndpointAccessCase("GET /api/enrollment/summary-count correct role -> 200", get("/api/enrollment/summary-count"), "ENROLLMENT_WRITE", 200),

                new EndpointAccessCase("POST /api/enrollment/register without token -> 401", post("/api/enrollment/register")
                        .header("Idempotency-Key", "req-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody), null, 401),
                new EndpointAccessCase("POST /api/enrollment/register wrong role -> 403", post("/api/enrollment/register")
                        .header("Idempotency-Key", "req-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody), "ENROLLMENT_READ", 403),
                new EndpointAccessCase("POST /api/enrollment/register correct role -> 201", post("/api/enrollment/register")
                        .header("Idempotency-Key", "req-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody), "ENROLLMENT_WRITE", 201),

                new EndpointAccessCase("POST /api/enrollment/{id}/cancel without token -> 401", post("/api/enrollment/" + enrollmentId + "/cancel"), null, 401),
                new EndpointAccessCase("POST /api/enrollment/{id}/cancel wrong role -> 403", post("/api/enrollment/" + enrollmentId + "/cancel"), "ENROLLMENT_READ", 403),
                new EndpointAccessCase("POST /api/enrollment/{id}/cancel correct role -> 200", post("/api/enrollment/" + enrollmentId + "/cancel"), "ENROLLMENT_WRITE", 200)
        );
    }

    private record EndpointAccessCase(
            String description,
            MockHttpServletRequestBuilder request,
            String authority,
            int expectedStatus
    ) {
        @Override
        public String toString() {
            return description;
        }
    }

    private static void setSecurityContext(String authority) {
        SecurityContextHolder.clearContext();
        if (authority == null) {
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test-user",
                        "not-used",
                        List.of(() -> authority)
                )
        );
    }
}
