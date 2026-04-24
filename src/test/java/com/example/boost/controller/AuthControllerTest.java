package com.example.boost.controller;

import com.example.boost.iam.api.AuthController;

import com.example.boost.TestDataFactory;
import com.example.boost.domain.dto.AuthResponse;
import com.example.boost.domain.dto.LoginRequest;
import com.example.boost.domain.dto.RefreshRequest;
import com.example.boost.domain.dto.RegisterRequest;
import com.example.boost.domain.dto.RegisterResponse;
import com.example.boost.common.exception.ConflictException;
import com.example.boost.common.exception.UnauthorizedException;
import com.example.boost.security.JwtService;
import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.AuthService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.example.boost.security.MethodSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    void registerSuccess() throws Exception {
        RegisterResponse response = RegisterResponse.builder()
                .username("demo")
                .email("demo@example.com")
                .roleCodes(java.util.Set.of("USER"))
                .build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("demo");
        request.setEmail("demo@example.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.username").value("demo"));
    }

    @Test
    void registerValidationError() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.errors.username").exists());
    }

    @Test
    void registerConflict() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ConflictException("Username already exists"));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("demo");
        request.setEmail("demo@example.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void registerMalformedJson() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }

    @Test
    void registerUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<register/>"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("Unsupported media type"));
    }

    @Test
    void registerForbiddenAdminAssignment() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new AccessDeniedException("Forbidden"));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("demo");
        request.setEmail("demo@example.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void registerUnauthorizedAdminAssignment() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UnauthorizedException("Unauthorized"));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("demo");
        request.setEmail("demo@example.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
    void loginSuccess() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(TestDataFactory.authResponse());

        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void loginValidationError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void loginUnauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new UnauthorizedException("Invalid credentials"));

        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void refreshSuccess() throws Exception {
        when(authService.refresh(any(RefreshRequest.class))).thenReturn(TestDataFactory.authResponse());

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void refreshUnauthorized() throws Exception {
        when(authService.refresh(any(RefreshRequest.class))).thenThrow(new UnauthorizedException("Invalid refresh token"));

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("bad");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void logoutSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutUnauthorizedWhenMalformedToken() throws Exception {
        doThrow(new UnauthorizedException("Invalid token"))
                .when(authService).logout(any(), any());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void logoutServerError() throws Exception {
        doThrow(new RuntimeException("boom"))
                .when(authService).logout(any(), any());

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }
}
