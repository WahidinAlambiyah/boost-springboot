package com.example.boost.controller;

import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.TokenService;
import com.example.boost.scheduling.api.AttendanceController;
import com.example.boost.scheduling.application.AttendanceService;
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

import java.time.LocalDate;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void getSummariesUnauthorized() throws Exception {
        mockMvc.perform(get("/api/attendance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ATTENDANCE_READ")
    void submitForbiddenWithoutMarkAuthority() throws Exception {
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
}
