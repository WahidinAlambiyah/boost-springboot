package com.example.boost.controller;

import com.example.boost.dto.AuthRequest;
import com.example.boost.dto.AuthResponse;
import com.example.boost.dto.ChangePasswordRequest;
import com.example.boost.dto.ForgotPasswordRequest;
import com.example.boost.dto.RegisterUserRequest;
import com.example.boost.dto.ResetPasswordRequest;
import com.example.boost.service.AuthService;
import com.example.boost.service.EmailVerificationService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(AuthService authService, EmailVerificationService emailVerificationService) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterUserRequest request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                              HttpServletRequest httpServletRequest) {
        String ip = httpServletRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = httpServletRequest.getRemoteAddr();
        }
        String userAgent = httpServletRequest.getHeader("User-Agent");
        authService.forgotPassword(request, ip, userAgent);
        return ResponseEntity.ok(Map.of("message", "Jika email terdaftar, kami telah mengirimkan tautan reset."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password berhasil diubah, silakan login dengan password baru."));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        emailVerificationService.verify(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Password berhasil diganti. Silakan login ulang jika diminta di perangkat lain."));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authService.logout(authorization.substring(7));
        }
        return ResponseEntity.noContent().build();
    }
}
