package com.example.boost.controller;

import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.TokenService;
import com.example.boost.scheduling.api.AttendanceController;
import com.example.boost.scheduling.application.AttendanceService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
@Tag("refactor-gate")
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AttendanceService attendanceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser(authorities = "ATTENDANCE_MARK")
    void submitSuccess() throws Exception {
        doNothing().when(attendanceService).submit(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());

        mockMvc.perform(post("/api/attendance/submit")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "classGroupId", java.util.UUID.randomUUID(),
                                "sessionDate", LocalDate.now(),
                                "presentCount", 10,
                                "absentCount", 2
                        ))))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data").value("queued"));
    }

    @Test
    @WithMockUser(authorities = "ATTENDANCE_MARK")
    void submitOtherInstructorClassShouldReturnForbidden() throws Exception {
        doThrow(new AccessDeniedException("Forbidden"))
                .when(attendanceService)
                .submit(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt()
                );

        mockMvc.perform(post("/api/attendance/submit")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "classGroupId", java.util.UUID.randomUUID(),
                                "sessionDate", LocalDate.now(),
                                "presentCount", 10,
                                "absentCount", 2
                        ))))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("endpointSecurityCases")
    void domainEndpointsMustCoverUnauthorizedForbiddenAndSuccess(EndpointAccessCase testCase) throws Exception {
        when(attendanceService.getSummaries()).thenReturn(List.of());
        when(attendanceService.getWritableSummaryCount()).thenReturn(0L);
        doNothing().when(attendanceService).submit(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt()
        );
        when(attendanceService.getSessionAttendances(org.mockito.ArgumentMatchers.any(UUID.class))).thenReturn(List.of());
        doNothing().when(attendanceService).upsertStudentAttendance(
                org.mockito.ArgumentMatchers.any(UUID.class),
                org.mockito.ArgumentMatchers.any(UUID.class),
                org.mockito.ArgumentMatchers.any()
        );
        when(attendanceService.upsertSessionAttendanceBatch(org.mockito.ArgumentMatchers.any(UUID.class), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.example.boost.domain.dto.AttendanceBatchUpsertResponse(1, "ok"));

        MockHttpServletRequestBuilder request = testCase.request();
        setSecurityContext(testCase.authority());

        mockMvc.perform(request)
                .andExpect(status().is(testCase.expectedStatus()));
        SecurityContextHolder.clearContext();
    }

    private static Stream<EndpointAccessCase> endpointSecurityCases() {
        String submitBody = "{\"classGroupId\":\"11111111-1111-1111-1111-111111111111\",\"sessionDate\":\"2026-01-01\",\"presentCount\":10,\"absentCount\":2}";
        String upsertBody = "{\"studentId\":\"11111111-1111-1111-1111-111111111111\",\"attendanceStatus\":\"PRESENT\",\"remarks\":\"on time\"}";
        String upsertStudentBody = "{\"attendanceStatus\":\"PRESENT\",\"remarks\":\"on time\"}";
        String batchBody = "{\"records\":[{\"studentId\":\"11111111-1111-1111-1111-111111111111\",\"attendance\":{\"attendanceStatus\":\"PRESENT\",\"remarks\":\"on time\"}}]}";
        return Stream.of(
                new EndpointAccessCase("GET /api/attendance without token -> 401", get("/api/attendance"), null, 401),
                new EndpointAccessCase("GET /api/attendance wrong role -> 403", get("/api/attendance"), "ATTENDANCE_MARK", 403),
                new EndpointAccessCase("GET /api/attendance correct role -> 200", get("/api/attendance"), "ATTENDANCE_READ", 200),

                new EndpointAccessCase("GET /api/attendance/summary-count without token -> 401", get("/api/attendance/summary-count"), null, 401),
                new EndpointAccessCase("GET /api/attendance/summary-count wrong role -> 403", get("/api/attendance/summary-count"), "ATTENDANCE_READ", 403),
                new EndpointAccessCase("GET /api/attendance/summary-count correct role -> 200", get("/api/attendance/summary-count"), "ATTENDANCE_MARK", 200),

                new EndpointAccessCase("POST /api/attendance/submit without token -> 401", post("/api/attendance/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody), null, 401),
                new EndpointAccessCase("POST /api/attendance/submit wrong role -> 403", post("/api/attendance/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody), "ATTENDANCE_READ", 403),
                new EndpointAccessCase("POST /api/attendance/submit correct role -> 202", post("/api/attendance/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitBody), "ATTENDANCE_MARK", 202)
                ,
                new EndpointAccessCase("GET /api/attendance/sessions/{id} correct role -> 200",
                        get("/api/attendance/sessions/11111111-1111-1111-1111-111111111111"),
                        "ATTENDANCE_READ", 200),
                new EndpointAccessCase("POST /api/attendance/sessions/{id}/students correct role -> 200",
                        post("/api/attendance/sessions/11111111-1111-1111-1111-111111111111/students")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(upsertBody),
                        "ATTENDANCE_MARK", 200),
                new EndpointAccessCase("PUT /api/attendance/sessions/{id}/students/{studentId} correct role -> 200",
                        put("/api/attendance/sessions/11111111-1111-1111-1111-111111111111/students/11111111-1111-1111-1111-111111111111")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(upsertStudentBody),
                        "ATTENDANCE_MARK", 200)

                ,new EndpointAccessCase("GET /api/attendance/sessions/{id} without token -> 401",
                        get("/api/attendance/sessions/11111111-1111-1111-1111-111111111111"),
                        null, 401),
                new EndpointAccessCase("GET /api/attendance/sessions/{id} wrong role -> 403",
                        get("/api/attendance/sessions/11111111-1111-1111-1111-111111111111"),
                        "ATTENDANCE_MARK", 403),
                new EndpointAccessCase("POST /api/attendance/sessions/{id}/students wrong role -> 403",
                        post("/api/attendance/sessions/11111111-1111-1111-1111-111111111111/students")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(upsertBody),
                        "ATTENDANCE_READ", 403),
                new EndpointAccessCase("PUT /api/attendance/sessions/{id}/students/{studentId} wrong role -> 403",
                        put("/api/attendance/sessions/11111111-1111-1111-1111-111111111111/students/11111111-1111-1111-1111-111111111111")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(upsertStudentBody),
                        "ATTENDANCE_READ", 403),
                new EndpointAccessCase("POST /api/attendance/sessions/{id}/batch without token -> 401",
                        post("/api/attendance/sessions/11111111-1111-1111-1111-111111111111/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(batchBody),
                        null, 401),
                new EndpointAccessCase("POST /api/attendance/sessions/{id}/batch wrong role -> 403",
                        post("/api/attendance/sessions/11111111-1111-1111-1111-111111111111/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(batchBody),
                        "ATTENDANCE_READ", 403),
                new EndpointAccessCase("POST /api/attendance/sessions/{id}/batch correct role -> 200",
                        post("/api/attendance/sessions/11111111-1111-1111-1111-111111111111/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(batchBody),
                        "ATTENDANCE_MARK", 200)
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
