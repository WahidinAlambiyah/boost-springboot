package com.example.graphqlusers.service;

import com.example.graphqlusers.app.AppException;
import com.example.graphqlusers.app.AuthPayload;
import com.example.graphqlusers.app.ErrorCodes;
import com.example.graphqlusers.app.RegisterInput;
import com.example.graphqlusers.app.Role;
import com.example.graphqlusers.app.User;
import com.example.graphqlusers.auth.PasswordHasher;
import com.example.graphqlusers.auth.JwtService;
import com.example.graphqlusers.redis.RefreshTokenStore;
import com.example.graphqlusers.repository.UserRepository;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;

    public AuthService(UserRepository userRepository, JwtService jwtService, RefreshTokenStore refreshTokenStore) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenStore = refreshTokenStore;
    }

    public AuthPayload register(RegisterInput input) {
        if (input.username() == null || input.username().isBlank()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Username is required");
        }
        if (input.email() == null || input.email().isBlank() || !EMAIL_PATTERN.matcher(input.email()).matches()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Valid email is required");
        }
        if (input.password() == null || input.password().length() < 8) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Password must be at least 8 characters");
        }
        if (userRepository.findByUsername(input.username()).isPresent()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Username already exists");
        }
        if (userRepository.findByEmail(input.email()).isPresent()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Email already exists");
        }
        Instant now = Instant.now();
        User user = new User(UUID.randomUUID(), input.username(), input.email(), input.fullName(),
                PasswordHasher.hash(input.password()), Set.of(Role.USER), now, now);
        try {
            userRepository.create(user);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.sql.SQLException sql && "23505".equals(sql.getSQLState())) {
                throw new AppException(ErrorCodes.BAD_REQUEST, "Username or email already exists");
            }
            throw e;
        }
        return issueTokens(user);
    }

    public AuthPayload login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Username or email is required");
        }
        if (password == null || password.isBlank()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Password is required");
        }
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new AppException(ErrorCodes.UNAUTHORIZED, "Invalid credentials"));
        if (!PasswordHasher.verify(password, user.passwordHash())) {
            throw new AppException(ErrorCodes.UNAUTHORIZED, "Invalid credentials");
        }
        return issueTokens(user);
    }

    public AuthPayload refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Refresh token is required");
        }
        UUID userId = refreshTokenStore.getUserId(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCodes.UNAUTHORIZED, "Refresh token expired"));
        refreshTokenStore.revoke(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCodes.UNAUTHORIZED, "User not found"));
        return issueTokens(user);
    }

    public boolean logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Refresh token is required");
        }
        refreshTokenStore.revoke(refreshToken);
        return true;
    }

    private AuthPayload issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.id(), user.roles());
        String refreshToken = refreshTokenStore.issue(user.id());
        return new AuthPayload(accessToken, refreshToken, user);
    }
}
