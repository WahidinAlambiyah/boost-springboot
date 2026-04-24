package com.example.boost.service;

import com.example.boost.iam.application.AuditLogService;
import com.example.boost.iam.application.AuthService;
import com.example.boost.iam.application.TokenService;

import com.example.boost.config.JwtProperties;
import com.example.boost.domain.dto.AuthResponse;
import com.example.boost.domain.dto.LoginRequest;
import com.example.boost.domain.dto.RefreshRequest;
import com.example.boost.domain.entity.AuditLog;
import com.example.boost.domain.entity.User;
import com.example.boost.common.exception.UnauthorizedException;
import com.example.boost.iam.infrastructure.PermissionRepository;
import com.example.boost.iam.infrastructure.RoleRepository;
import com.example.boost.iam.infrastructure.UserRepository;
import com.example.boost.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenService tokenService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUpAuditLog() {
        AuditLogService.AuditLogBuilder builder = mock(AuditLogService.AuditLogBuilder.class, Mockito.RETURNS_SELF);
        Mockito.lenient().when(builder.save()).thenReturn(new AuditLog());
        Mockito.lenient().when(auditLogService.securityEvent(any())).thenReturn(builder);
        Mockito.lenient().when(auditLogService.rbacEvent(any())).thenReturn(builder);
        Mockito.lenient().when(auditLogService.dataEvent(any())).thenReturn(builder);
    }

    @Test
    void loginSuccessResetsFailureCount() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("demo");
        user.setPasswordHash("hash");
        user.setActive(true);
        user.setFailedLoginCount(2);
        user.setLockedUntil(OffsetDateTime.now().minusMinutes(1));

        when(userRepository.findByUsernameIgnoreCase("demo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hash")).thenReturn(true);
        when(roleRepository.findCodesByUserId(user.getId())).thenReturn(List.of("USER"));
        when(permissionRepository.findCodesByUserId(user.getId())).thenReturn(List.of("USER_READ"));
        when(jwtService.generateAccessToken(any(), any(), any(), any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any(), any())).thenReturn("refresh");
        when(jwtProperties.getRefreshTokenTtlDays()).thenReturn(7L);

        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        when(jws.getBody()).thenReturn(claims);
        when(claims.getId()).thenReturn("jti");
        when(jwtService.parseToken("refresh")).thenReturn(jws);

        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("Password123!");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access");
        verify(userRepository).save(user);
        assertThat(user.getFailedLoginCount()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void loginFailureIncrementsCountAndLocks() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("demo");
        user.setPasswordHash("hash");
        user.setActive(true);
        user.setFailedLoginCount(4);

        when(userRepository.findByUsernameIgnoreCase("demo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("bad");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);

        verify(userRepository).save(user);
        assertThat(user.getFailedLoginCount()).isEqualTo(5);
        assertThat(user.getLockedUntil()).isNotNull();
    }

    @Test
    void logoutMalformedTokenThrowsUnauthorized() {
        when(jwtService.parseToken("bad")).thenThrow(new JwtException("bad"));

        assertThatThrownBy(() -> authService.logout("bad", null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refreshSuccessReturnsAccessToken() {
        Claims claims = mock(Claims.class);
        when(claims.get("typ", String.class)).thenReturn("refresh");
        when(claims.get("uid", String.class)).thenReturn(UUID.randomUUID().toString());
        when(claims.getId()).thenReturn("jti");
        Jws<Claims> jws = mock(Jws.class);
        when(jws.getBody()).thenReturn(claims);
        when(jwtService.parseToken("refresh")).thenReturn(jws);
        when(tokenService.isRefreshTokenValid(any(), any(), any())).thenReturn(true);

        User user = new User();
        user.setId(UUID.fromString(claims.get("uid", String.class)));
        user.setUsername("demo");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findCodesByUserId(user.getId())).thenReturn(List.of("USER"));
        when(permissionRepository.findCodesByUserId(user.getId())).thenReturn(List.of());
        when(jwtService.generateAccessToken(any(), any(), any(), any())).thenReturn("access");

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh");

        AuthResponse response = authService.refresh(request);

        assertThat(response.getAccessToken()).isEqualTo("access");
    }
}
