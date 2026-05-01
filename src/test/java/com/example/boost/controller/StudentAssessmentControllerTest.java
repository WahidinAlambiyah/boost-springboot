package com.example.boost.controller;

import com.example.boost.assessment.api.StudentAssessmentController;
import com.example.boost.assessment.application.StudentAssessmentService;
import com.example.boost.domain.dto.AssessmentCreateRequest;
import com.example.boost.domain.dto.AssessmentDetailResponse;
import com.example.boost.domain.dto.AssessmentScoreRequest;
import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.TokenService;
import com.example.boost.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentAssessmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
class StudentAssessmentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentAssessmentService studentAssessmentService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser(authorities = "ASSESSMENT_WRITE")
    void createAssessmentSuccess() throws Exception {
        UUID assessmentId = UUID.randomUUID();
        when(studentAssessmentService.create(any(), any(AssessmentCreateRequest.class))).thenReturn(response(assessmentId, UUID.randomUUID()));

        mockMvc.perform(post("/api/assessments")
                        .param("academyId", UUID.randomUUID().toString())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssessmentCreateRequest(
                                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now(), "ok",
                                List.of(new AssessmentScoreRequest("FOCUS", BigDecimal.ONE, "good"))
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Assessment created"))
                .andExpect(jsonPath("$.data.id").value(assessmentId.toString()));
    }

    @Test
    @WithMockUser(authorities = "ASSESSMENT_READ")
    void listAndFilterAssessmentSuccess() throws Exception {
        UUID studentId = UUID.randomUUID();
        when(studentAssessmentService.list(any(), any(), any(), any())).thenReturn(List.of(response(UUID.randomUUID(), studentId)));

        mockMvc.perform(get("/api/assessments")
                        .param("studentId", studentId.toString())
                        .param("from", "2026-01-01T00:00:00Z")
                        .param("to", "2026-01-31T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Assessments retrieved"))
                .andExpect(jsonPath("$.data[0].studentId").value(studentId.toString()));
    }

    private AssessmentDetailResponse response(UUID id, UUID studentId) {
        return new AssessmentDetailResponse(id, UUID.randomUUID(), studentId, UUID.randomUUID(), OffsetDateTime.now(), "ok",
                List.of(new AssessmentDetailResponse.AssessmentScoreDetailResponse(
                        UUID.randomUUID(), "FOCUS", "Focus", 5, "4.0", "steady"
                )));
    }
}
