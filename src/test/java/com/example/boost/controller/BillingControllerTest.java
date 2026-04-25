package com.example.boost.controller;

import com.example.boost.billing.api.BillingController;
import com.example.boost.billing.application.BillingService;
import com.example.boost.domain.dto.PaymentRequest;
import com.example.boost.domain.dto.PaymentResponse;
import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.TokenService;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
@Tag("refactor-gate")
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillingService billingService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser(authorities = "BILLING_WRITE")
    void paySuccess() throws Exception {
        UUID paymentId = UUID.randomUUID();
        PaymentResponse paymentResponse = new PaymentResponse(
                paymentId,
                UUID.randomUUID(),
                BigDecimal.valueOf(250000),
                "SUCCESS"
        );
        when(billingService.pay(any(PaymentRequest.class), anyString())).thenReturn(paymentResponse);

        mockMvc.perform(post("/api/billing/pay")
                        .header("Idempotency-Key", " invoice-1 ")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "invoiceId", UUID.randomUUID(),
                                "amount", BigDecimal.valueOf(250000)
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.paymentId").value(paymentId.toString()))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    void getSummariesUnauthorized() throws Exception {
        mockMvc.perform(get("/api/billing"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "BILLING_READ")
    void payForbiddenWithoutWriteAuthority() throws Exception {
        mockMvc.perform(post("/api/billing/pay")
                        .header("Idempotency-Key", "invoice-2")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "invoiceId", UUID.randomUUID(),
                                "amount", BigDecimal.valueOf(50000)
                        ))))
                .andExpect(status().isForbidden());
    }
}
