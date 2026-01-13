package com.alambiyah.userauth.application.service;

import com.alambiyah.userauth.domain.entity.User;
import com.alambiyah.userauth.dto.AuthResponse;
import com.alambiyah.userauth.dto.LoginRequest;
import com.alambiyah.userauth.dto.RegisterRequest;
import com.alambiyah.userauth.framework.security.PasswordService;
import com.alambiyah.userauth.framework.security.TokenService;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {
    private final UserService userService;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    @Inject
    public AuthService(UserService userService, PasswordService passwordService, TokenService tokenService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = userService.register(request.username(), request.email(), request.fullName(), request.password());
        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userService.findByUsernameOrEmail(request.usernameOrEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!user.isActive) {
            throw new UnauthorizedException("User is inactive");
        }
        if (!passwordService.matches(request.password(), user.passwordHash)) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        String userId = tokenService.consumeRefreshToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        User user = userService.getById(java.util.UUID.fromString(userId));
        if (!user.isActive) {
            throw new UnauthorizedException("User is inactive");
        }
        return issueTokens(user);
    }

    public void logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }
}
