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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

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
    void getSummariesUnauthorized() throws Exception {
        mockMvc.perform(get("/api/enrollment"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "USER_READ")
    void getSummariesForbiddenWithoutAuthority() throws Exception {
        mockMvc.perform(get("/api/enrollment"))
                .andExpect(status().isForbidden());
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
}
