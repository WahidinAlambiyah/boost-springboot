package com.example.purchaseorder.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Test
    void shouldGenerateAndValidateToken() {
        String secret = Base64.getEncoder().encodeToString("test-secret-key-test-secret-key".getBytes());
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expirationMillis", 60000L);

        String token = jwtService.generateToken(org.springframework.security.core.userdetails.User
                .withUsername("user@example.com")
                .password("password")
                .roles("USER")
                .build());

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@example.com");
        assertThat(jwtService.isTokenValid(token, org.springframework.security.core.userdetails.User
                .withUsername("user@example.com")
                .password("password")
                .roles("USER")
                .build())).isTrue();
    }
}
