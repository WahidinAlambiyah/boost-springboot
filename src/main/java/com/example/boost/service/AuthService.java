package com.example.boost.service;

import com.example.boost.config.JwtProperties;
import com.example.boost.config.LoginProperties;
import com.example.boost.domain.dto.AuthResponse;
import com.example.boost.domain.dto.LoginRequest;
import com.example.boost.domain.dto.RefreshRequest;
import com.example.boost.domain.dto.RegisterRequest;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.User;
import com.example.boost.exception.BadRequestException;
import com.example.boost.exception.UnauthorizedException;
import com.example.boost.repository.UserRepository;
import com.example.boost.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final LoginProperties loginProperties;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.USER));
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is inactive");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now())) {
            throw new UnauthorizedException("Account is locked. Try again later");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Invalid credentials");
        }

        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String token = request.getRefreshToken();
        Jws<Claims> jws = jwtService.parseToken(token);
        Claims claims = jws.getBody();
        String type = claims.get("typ", String.class);
        if (!"refresh".equals(type)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        String userId = claims.get("uid", String.class);
        String jti = claims.getId();
        if (!tokenService.isRefreshTokenValid(userId, jti, token)) {
            throw new UnauthorizedException("Refresh token not recognized");
        }

        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String roles = String.join(",", user.getRoles().stream().map(Enum::name).toList());
        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), user.getId().toString(), roles);
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .tokenType("Bearer")
                .build();
    }

    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null) {
            Jws<Claims> jws = jwtService.parseToken(accessToken);
            Claims claims = jws.getBody();
            tokenService.blacklistAccessToken(claims.getId(), claims.getExpiration());
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            Jws<Claims> refreshClaims = jwtService.parseToken(refreshToken);
            Claims claims = refreshClaims.getBody();
            String userId = claims.get("uid", String.class);
            String jti = claims.getId();
            tokenService.revokeRefreshToken(userId, jti);
        }
    }

    private AuthResponse issueTokens(User user) {
        String roles = String.join(",", user.getRoles().stream().map(Enum::name).toList());
        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getId().toString(), roles);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getId().toString(), roles);

        Jws<Claims> refreshClaims = jwtService.parseToken(refreshToken);
        Duration ttl = Duration.ofDays(jwtProperties.getRefreshTokenTtlDays());
        tokenService.storeRefreshToken(user.getId().toString(), refreshClaims.getBody().getId(), refreshToken, ttl);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(attempts);
        if (attempts >= loginProperties.getMaxFailedAttempts()) {
            user.setLockedUntil(OffsetDateTime.now().plusMinutes(loginProperties.getLockMinutes()));
            user.setFailedLoginCount(0);
        }
        userRepository.save(user);
    }
}
