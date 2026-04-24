package com.example.boost.iam.application;

import com.example.boost.config.JwtProperties;
import com.example.boost.domain.dto.AuthResponse;
import com.example.boost.domain.dto.LoginRequest;
import com.example.boost.domain.dto.RefreshRequest;
import com.example.boost.domain.dto.RegisterRequest;
import com.example.boost.domain.dto.RegisterResponse;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.User;
import com.example.boost.exception.ConflictException;
import com.example.boost.exception.UnauthorizedException;
import com.example.boost.exception.UnprocessableEntityException;
import com.example.boost.iam.infrastructure.PermissionRepository;
import com.example.boost.iam.infrastructure.RoleRepository;
import com.example.boost.iam.infrastructure.UserRepository;
import com.example.boost.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;
    private final AuditLogService auditLogService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(resolveRoles(Set.of("USER")));
        User saved = userRepository.save(user);

        return RegisterResponse.builder()
                .username(saved.getUsername())
                .email(saved.getEmail())
                .roleCodes(saved.getRoleCodes())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername()).orElse(null);
        if (user == null) {
            auditLogService.securityEvent("LOGIN_FAILED")
                    .actor(null, request.getUsername(), "UNKNOWN")
                    .statusFailure("Invalid credentials")
                    .metadata(java.util.Map.of("username", request.getUsername()))
                    .save();
            throw new UnauthorizedException("Invalid credentials");
        }

        if (!user.isActive()) {
            auditLogService.securityEvent("LOGIN_FAILED")
                    .actor(user.getId(), user.getUsername(), "USER")
                    .statusFailure("Account disabled")
                    .entity("USER", user.getId().toString())
                    .metadata(java.util.Map.of("reason", user.getDisabledReason()))
                    .save();
            throw new UnauthorizedException("Account is inactive");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(java.time.OffsetDateTime.now())) {
            auditLogService.securityEvent("LOGIN_FAILED")
                    .actor(user.getId(), user.getUsername(), "USER")
                    .statusFailure("Account locked")
                    .entity("USER", user.getId().toString())
                    .metadata(java.util.Map.of("lockedUntil", user.getLockedUntil()))
                    .save();
            throw new UnauthorizedException("Account is locked");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.setFailedLoginCount(user.getFailedLoginCount() + 1);
            user.setLastFailedLoginAt(java.time.OffsetDateTime.now());
            if (user.getFailedLoginCount() >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setLockedUntil(java.time.OffsetDateTime.now().plus(LOCK_DURATION));
                auditLogService.securityEvent("ACCOUNT_LOCKED")
                        .actor(user.getId(), user.getUsername(), "USER")
                        .statusFailure("Failed login threshold reached")
                        .entity("USER", user.getId().toString())
                        .metadata(java.util.Map.of(
                                "failedLoginCount", user.getFailedLoginCount(),
                                "lockedUntil", user.getLockedUntil()))
                        .save();
            }
            userRepository.save(user);
            auditLogService.securityEvent("LOGIN_FAILED")
                    .actor(user.getId(), user.getUsername(), "USER")
                    .statusFailure("Invalid credentials")
                    .entity("USER", user.getId().toString())
                    .metadata(java.util.Map.of("failedLoginCount", user.getFailedLoginCount()))
                    .save();
            throw new UnauthorizedException("Invalid credentials");
        }

        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(java.time.OffsetDateTime.now());
        userRepository.save(user);
        AuthResponse response = issueTokens(user);
        auditLogService.securityEvent("LOGIN_SUCCESS")
                .actor(user.getId(), user.getUsername(), "USER")
                .entity("USER", user.getId().toString())
                .statusSuccess()
                .save();
        return response;
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

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roles = roleRepository.findCodesByUserId(user.getId());
        List<String> permissions = permissionRepository.findCodesByUserId(user.getId());
        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), roles, permissions);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .tokenType("Bearer")
                .build();
    }

    public void logout(String accessToken, String refreshToken) {
        try {
            if (accessToken != null && !accessToken.isBlank()) {
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

            auditLogService.securityEvent("LOGOUT_SUCCESS")
                    .statusSuccess()
                    .save();
        } catch (io.jsonwebtoken.JwtException ex) {
            auditLogService.securityEvent("LOGOUT_FAILED")
                    .statusFailure("Invalid token")
                    .save();
            throw new UnauthorizedException("Invalid token");
        }
    }

    private AuthResponse issueTokens(User user) {
        List<String> roles = roleRepository.findCodesByUserId(user.getId());
        List<String> permissions = permissionRepository.findCodesByUserId(user.getId());

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getId(), roles, permissions);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername(), user.getId(), roles);

        Jws<Claims> refreshClaims = jwtService.parseToken(refreshToken);
        Duration ttl = Duration.ofDays(jwtProperties.getRefreshTokenTtlDays());
        tokenService.storeRefreshToken(user.getId().toString(), refreshClaims.getBody().getId(), refreshToken, ttl);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    private Set<Role> resolveRoles(Set<String> roleCodes) {
        Set<String> codes = roleCodes == null || roleCodes.isEmpty() ? Set.of("USER") : roleCodes;
        List<Role> roles = roleRepository.findByCodeIn(codes);
        if (roles.size() != codes.size()) {
            Set<String> found = new HashSet<>();
            for (Role role : roles) {
                found.add(role.getCode());
            }
            Set<String> missing = new HashSet<>(codes);
            missing.removeAll(found);
            throw new UnprocessableEntityException("Invalid role code(s): " + missing);
        }
        return new HashSet<>(roles);
    }

//    TODO: need confirm if unused for register public
//    private void enforcePrivilegedRoleAssignment(Set<String> roleCodes) {
//        if (roleCodes == null || !roleCodes.contains("ADMIN")) {
//            return;
//        }
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null
//                || !authentication.isAuthenticated()
//                || authentication instanceof AnonymousAuthenticationToken) {
//            throw new UnauthorizedException("Unauthorized");
//        }
//        boolean isAdmin = authentication.getAuthorities().stream()
//                .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));
//        if (!isAdmin) {
//            throw new AccessDeniedException("Forbidden");
//        }
//    }

}
