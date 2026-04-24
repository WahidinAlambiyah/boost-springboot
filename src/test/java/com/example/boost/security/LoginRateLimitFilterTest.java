package com.example.boost.security;

import com.example.boost.iam.application.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class LoginRateLimitFilterTest {

    private LoginRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        AuditLogService auditLogService = mock(AuditLogService.class, RETURNS_DEEP_STUBS);
        filter = new LoginRateLimitFilter(new ObjectMapper(), auditLogService);
    }

    @Test
    void registerIsRateLimitedAfterTenRequests() throws Exception {
        MockHttpServletResponse limitedResponse = null;
        for (int i = 0; i < 11; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");
            request.setRemoteAddr("10.10.10.10");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = new MockFilterChain();
            filter.doFilter(request, response, chain);
            if (i == 10) {
                limitedResponse = response;
            }
        }

        assertThat(limitedResponse).isNotNull();
        assertThat(limitedResponse.getStatus()).isEqualTo(429);
        assertThat(limitedResponse.getContentAsString())
                .contains("\"status\":429")
                .contains("Too many registration attempts. Please try again later.");
    }
}
