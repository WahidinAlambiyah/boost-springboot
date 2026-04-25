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

import java.math.BigDecimal;
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

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("endpointSecurityCases")
    void domainEndpointsMustCoverUnauthorizedForbiddenAndSuccess(EndpointAccessCase testCase) throws Exception {
        when(billingService.getSummaries()).thenReturn(List.of());
        when(billingService.getWritableSummaryCount()).thenReturn(0L);
        when(billingService.pay(any(PaymentRequest.class), anyString())).thenReturn(
                new PaymentResponse(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(50000), "SUCCESS")
        );

        MockHttpServletRequestBuilder request = testCase.request();
        setSecurityContext(testCase.authority());

        mockMvc.perform(request)
                .andExpect(status().is(testCase.expectedStatus()));
        SecurityContextHolder.clearContext();
    }

    private static Stream<EndpointAccessCase> endpointSecurityCases() {
        String payBody = "{\"invoiceId\":\"11111111-1111-1111-1111-111111111111\",\"amount\":50000}";
        return Stream.of(
                new EndpointAccessCase("GET /api/billing without token -> 401", get("/api/billing"), null, 401),
                new EndpointAccessCase("GET /api/billing wrong role -> 403", get("/api/billing"), "BILLING_WRITE", 403),
                new EndpointAccessCase("GET /api/billing correct role -> 200", get("/api/billing"), "BILLING_READ", 200),

                new EndpointAccessCase("GET /api/billing/summary-count without token -> 401", get("/api/billing/summary-count"), null, 401),
                new EndpointAccessCase("GET /api/billing/summary-count wrong role -> 403", get("/api/billing/summary-count"), "BILLING_READ", 403),
                new EndpointAccessCase("GET /api/billing/summary-count correct role -> 200", get("/api/billing/summary-count"), "BILLING_WRITE", 200),

                new EndpointAccessCase("POST /api/billing/pay without token -> 401", post("/api/billing/pay")
                        .header("Idempotency-Key", "invoice-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payBody), null, 401),
                new EndpointAccessCase("POST /api/billing/pay wrong role -> 403", post("/api/billing/pay")
                        .header("Idempotency-Key", "invoice-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payBody), "BILLING_READ", 403),
                new EndpointAccessCase("POST /api/billing/pay correct role -> 201", post("/api/billing/pay")
                        .header("Idempotency-Key", "invoice-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payBody), "BILLING_WRITE", 201)
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
