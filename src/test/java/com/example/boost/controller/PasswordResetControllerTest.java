package com.example.boost.controller;

import com.example.boost.iam.api.PasswordResetController;

import com.example.boost.domain.dto.PasswordResetConfirmRequest;
import com.example.boost.domain.dto.PasswordResetRequestDto;
import com.example.boost.domain.dto.PasswordResetResendRequest;
import com.example.boost.exception.BadRequestException;
import com.example.boost.exception.TooManyRequestsException;
import com.example.boost.security.JwtService;
import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.PasswordResetService;
import com.example.boost.iam.application.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    void requestResetSuccess() throws Exception {
        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setEmail("demo@example.com");
        request.setMode("OTP");

        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email exists, reset instructions were sent"));
    }

    @Test
    void requestResetValidationError() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestResetRateLimited() throws Exception {
        doThrow(new TooManyRequestsException("Too many reset requests. Please try again later."))
                .when(passwordResetService).requestReset(any(), any());

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setEmail("demo@example.com");
        request.setMode("OTP");

        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Too many reset requests. Please try again later."));
    }

    @Test
    void resendOtpSuccess() throws Exception {
        PasswordResetResendRequest request = new PasswordResetResendRequest();
        request.setResetRequestId(UUID.randomUUID());

        mockMvc.perform(post("/api/auth/password-reset/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP resent"));
    }

    @Test
    void confirmWithInvalidPayload() throws Exception {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setNewPassword("Password123!");

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid password reset payload"));
    }

    @Test
    void confirmServerError() throws Exception {
        doThrow(new BadRequestException("Invalid reset token"))
                .when(passwordResetService).confirmWithToken(any(), any());

        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken("token");
        request.setNewPassword("Password123!");

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid reset token"));
    }
}
