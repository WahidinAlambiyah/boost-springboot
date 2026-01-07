package com.example.boost.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.boost.domain.User;
import com.example.boost.dto.AuthRequest;
import com.example.boost.dto.AuthResponse;
import com.example.boost.dto.ChangePasswordRequest;
import com.example.boost.dto.ForgotPasswordRequest;
import com.example.boost.dto.RegisterUserRequest;
import com.example.boost.dto.ResetPasswordRequest;
import com.example.boost.security.JwtTokenProvider;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final SessionService sessionService;
    private final PasswordResetService passwordResetService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       UserService userService,
                       SessionService sessionService,
                       PasswordResetService passwordResetService,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.sessionService = sessionService;
        this.passwordResetService = passwordResetService;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterUserRequest request) {
        userService.register(request);
    }

    public void forgotPassword(ForgotPasswordRequest request, String requestIp, String userAgent) {
        passwordResetService.requestReset(request.getEmail(), requestIp, userAgent);
    }

    public void resetPassword(ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
    }

    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Pengguna tidak ditemukan.");
        }

        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Pengguna tidak ditemukan."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password lama tidak sesuai.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Konfirmasi password tidak sesuai.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password baru tidak boleh sama dengan password lama.");
        }

        PasswordValidator.validatePasswordRules(request.getNewPassword());

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setPasswordChangedAt(Instant.now());
        userService.save(user);
        sessionService.invalidateUserSessions(user.getId());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username atau password salah."));

        userService.ensureLoginAllowed(user);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            userService.recordSuccessfulLogin(user);
            UUID userId = user.getId();
            String sessionId = UUID.randomUUID().toString();
            String token = jwtTokenProvider.generateToken(authentication.getName(), sessionId);
            Instant expiresAt = jwtTokenProvider.getExpiryInstant();
            sessionService.storeSession(sessionId, userId, Duration.ofMillis(jwtTokenProvider.getExpirationMillis()));
            Set<String> roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet());
            return new AuthResponse(token, expiresAt, sessionId, roles, userService.toResponse(user));
        } catch (AuthenticationException ex) {
            userService.recordFailedLogin(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username atau password salah.");
        }
    }

    public void logout(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            String sessionId = jwtTokenProvider.getSessionId(token);
            sessionService.invalidateSession(sessionId);
        }
    }
}
