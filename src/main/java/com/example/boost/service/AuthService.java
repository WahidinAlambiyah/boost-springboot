package com.example.boost.service;

import com.example.boost.config.JwtProperties;
import com.example.boost.domain.dto.AuthResponse;
import com.example.boost.domain.dto.LoginRequest;
import com.example.boost.domain.dto.RefreshRequest;
import com.example.boost.domain.dto.RegisterRequest;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.User;
import com.example.boost.exception.ConflictException;
import com.example.boost.exception.NotFoundException;
import com.example.boost.exception.UnauthorizedException;
import com.example.boost.repository.PermissionRepository;
import com.example.boost.repository.RoleRepository;
import com.example.boost.repository.UserRepository;
import com.example.boost.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
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
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(resolveRoles(request.getRoleCodes()));
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

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

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
            throw new NotFoundException("Role codes not found: " + missing);
        }
        return new HashSet<>(roles);
    }
}
