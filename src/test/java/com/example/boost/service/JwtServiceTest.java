package com.example.boost.service;

import com.example.boost.config.JwtProperties;
import com.example.boost.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void generateAccessTokenHasTwoPeriods() {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("test");
        properties.setSecret("0123456789abcdef0123456789abcdef");
        properties.setAccessTokenTtlMinutes(15);
        properties.setRefreshTokenTtlDays(7);

        JwtService jwtService = new JwtService(properties);
        String token = jwtService.generateAccessToken("demo", UUID.randomUUID(), List.of("USER"), List.of("USER_READ"));

        assertThat(token.chars().filter(ch -> ch == '.').count()).isEqualTo(2);
    }

    @Test
    void parseTokenRejectsMalformed() {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("test");
        properties.setSecret("0123456789abcdef0123456789abcdef");
        properties.setAccessTokenTtlMinutes(15);
        properties.setRefreshTokenTtlDays(7);

        JwtService jwtService = new JwtService(properties);

        assertThatThrownBy(() -> jwtService.parseToken("abc"))
                .isInstanceOf(JwtException.class);
    }
}
