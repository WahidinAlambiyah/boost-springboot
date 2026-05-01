package com.example.boost.controller;

import com.example.boost.domain.dto.StudentProgressReportResponse;
import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.TokenService;
import com.example.boost.reporting.api.StudentProgressReportController;
import com.example.boost.reporting.application.StudentProgressReportService;
import com.example.boost.scheduling.api.AttendanceController;
import com.example.boost.scheduling.application.AttendanceService;
import com.example.boost.security.JwtService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({StudentProgressReportController.class, AttendanceController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
class StudentProgressReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentProgressReportService studentProgressReportService;
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
    @WithMockUser(authorities = "ATTENDANCE_READ")
    void reportShouldReturnCombinedAttendanceAndAssessmentData() throws Exception {
        UUID studentId = UUID.randomUUID();
        when(studentProgressReportService.getStudentProgressReport(any(), any(), any())).thenReturn(report(studentId));

        mockMvc.perform(get("/api/reports/students/{studentId}/progress", studentId)
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendance.totalSessions").value(8))
                .andExpect(jsonPath("$.data.skillProgress.averageScore").value(4.25));
    }

    @Test
    @WithMockUser(authorities = "ATTENDANCE_READ")
    void regressionExistingAttendanceEndpointStillWorks() throws Exception {
        when(attendanceService.getSummaries()).thenReturn(List.of());

        mockMvc.perform(get("/api/attendance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Attendance records retrieved"));
    }

    private StudentProgressReportResponse report(UUID studentId) {
        return new StudentProgressReportResponse(
                new StudentProgressReportResponse.StudentIdentity(studentId, "S-001", "Budi", "Bud", "L2"),
                new StudentProgressReportResponse.Period(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31")),
                new StudentProgressReportResponse.AttendanceSummary(8, 6, 1, 1),
                new StudentProgressReportResponse.SkillProgress(BigDecimal.valueOf(4.5), BigDecimal.valueOf(4.25), BigDecimal.valueOf(4.0), StudentProgressReportResponse.Trend.UP),
                List.of(new StudentProgressReportResponse.CoachNoteBySessionDate(LocalDate.parse("2026-01-20"), "progress bagus"))
        );
    }
}
