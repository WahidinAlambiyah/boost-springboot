package com.example.purchaseorder.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Test
    void shouldGenerateAndValidateToken() {
        // HS256 requires a secret that is at least 256 bits (32 bytes). Generating via Keys.secretKeyFor
        // guarantees the key meets the algorithm's minimum length so the test cannot regress by
        // accidentally using a shorter constant and triggering WeakKeyException.
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String secret = Base64.getEncoder().encodeToString(key.getEncoded());
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expirationMillis", 60000L);

        UserDetails user = User.withUsername("user@example.com")
                .password("password")
                .roles("USER")
                .build();

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@example.com");
        assertThat(jwtService.extractRoles(token)).containsExactly("ROLE_USER");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();

        UserDetails mismatchedUser = User.withUsername("user@example.com")
                .password("password")
                .roles("ADMIN")
                .build();

        assertThat(jwtService.isTokenValid(token, mismatchedUser)).isFalse();
    }
}
