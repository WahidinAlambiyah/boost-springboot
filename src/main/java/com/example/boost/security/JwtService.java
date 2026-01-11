package com.example.boost.security;

import com.example.boost.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    public String generateAccessToken(String username, String userId, String roles) {
        return buildToken(username, userId, roles, jwtProperties.getAccessTokenTtlMinutes(), ChronoUnit.MINUTES, "access");
    }

    public String generateRefreshToken(String username, String userId, String roles) {
        return buildToken(username, userId, roles, jwtProperties.getRefreshTokenTtlDays(), ChronoUnit.DAYS, "refresh");
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseClaimsJws(token);
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private String buildToken(String username, String userId, String roles, long ttl, ChronoUnit unit, String type) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setIssuer(jwtProperties.getIssuer())
                .setSubject(username)
                .setId(jti)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(ttl, unit)))
                .addClaims(Map.of(
                        "uid", userId,
                        "roles", roles,
                        "typ", type
                ))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        String secret = jwtProperties.getSecret();
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(\"JWT secret must be at least 32 bytes for HS256\");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
