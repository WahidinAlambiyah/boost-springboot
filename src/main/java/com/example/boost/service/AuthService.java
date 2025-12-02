package com.example.boost.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.boost.dto.AuthRequest;
import com.example.boost.dto.AuthResponse;
import com.example.boost.dto.RegisterUserRequest;
import com.example.boost.security.JwtTokenProvider;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final SessionService sessionService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       UserService userService,
                       SessionService sessionService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.sessionService = sessionService;
    }

    public void register(RegisterUserRequest request) {
        userService.register(request);
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UUID userId = userService.findByUsername(authentication.getName())
                .map(com.example.boost.domain.User::getId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        String sessionId = UUID.randomUUID().toString();
        String token = jwtTokenProvider.generateToken(authentication.getName(), sessionId);
        Instant expiresAt = jwtTokenProvider.getExpiryInstant();
        sessionService.storeSession(sessionId, userId, Duration.ofMillis(jwtTokenProvider.getExpirationMillis()));
        return new AuthResponse(token, expiresAt, sessionId);
    }

    public void logout(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            String sessionId = jwtTokenProvider.getSessionId(token);
            sessionService.invalidateSession(sessionId);
        }
    }
}
