package com.example.boost.controller;

import com.example.boost.billing.api.CoachPayrollController;
import com.example.boost.billing.application.CoachPayrollService;
import com.example.boost.common.exception.BadRequestException;
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

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoachPayrollController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
class CoachPayrollControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CoachPayrollService coachPayrollService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenService tokenService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private AuditLogService auditLogService;

    @Test
    @WithMockUser(authorities = "PAYROLL_WRITE")
    void updateItemShouldRejectWhenPeriodPaid() throws Exception {
        when(coachPayrollService.updateItem(any(), any(), any()))
                .thenThrow(new BadRequestException("Cannot edit paid payroll period"));

        mockMvc.perform(put("/api/payroll/coach/{payrollPeriodId}/items/{itemId}", UUID.randomUUID(), UUID.randomUUID())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "bonusAmount", 10000,
                                "deductionAmount", 2000
                        ))))
                .andExpect(status().isBadRequest());
    }
}
