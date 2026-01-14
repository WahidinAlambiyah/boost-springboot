package com.example.graphqlusers.auth;

import com.example.graphqlusers.app.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class JwtService {
    private final SecretKey secretKey;
    private final Duration ttl;

    public JwtService(String secret, Duration ttl) {
        byte[] keyBytes;
        if (secret.length() >= 32) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        } else {
            keyBytes = Decoders.BASE64.decode(secret);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.ttl = ttl;
    }

    public String generateAccessToken(UUID userId, Set<Role> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .claim("roles", roles.stream().map(Enum::name).collect(Collectors.toList()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public AuthContext parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        UUID userId = UUID.fromString(claims.getSubject());
        @SuppressWarnings("unchecked")
        var roleList = (Iterable<String>) claims.get("roles", Iterable.class);
        Set<Role> roles = new java.util.HashSet<>();
        if (roleList != null) {
            for (String role : roleList) {
                roles.add(Role.valueOf(role));
            }
        }
        return new AuthContext(userId, roles, token);
    }
}
