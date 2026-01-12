package com.alambiyah.userauth.framework.security;

import com.alambiyah.userauth.domain.entity.User;
import io.quarkus.redis.client.RedisClient;
import io.vertx.redis.client.Response;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@ApplicationScoped
public class TokenService {
    private static final String REFRESH_PREFIX = "rt:";

    private final RedisClient redisClient;
    private final SecureRandom secureRandom = new SecureRandom();

    @ConfigProperty(name = "app.jwt.access-token-ttl")
    Duration accessTokenTtl;

    @ConfigProperty(name = "app.jwt.refresh-token-ttl")
    Duration refreshTokenTtl;

    @ConfigProperty(name = "app.jwt.issuer")
    String issuer;

    @Inject
    public TokenService(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTokenTtl);
        return Jwt.subject(user.id.toString())
                .preferredUserName(user.username)
                .issuer(issuer)
                .groups(user.roles.stream().map(Enum::name).toList())
                .issuedAt(now)
                .expiresAt(exp)
                .sign();
    }

    public String generateRefreshToken(User user) {
        String token = generateOpaqueToken();
        String key = REFRESH_PREFIX + hashToken(token);
        redisClient.setex(key, String.valueOf(refreshTokenTtl.toSeconds()), user.id.toString());
        return token;
    }

    public Optional<String> consumeRefreshToken(String token) {
        String key = REFRESH_PREFIX + hashToken(token);
        Response response = redisClient.get(key);
        if (response == null) {
            return Optional.empty();
        }
        String userId = response.toString();
        redisClient.del(key);
        return Optional.of(userId);
    }

    public void revokeRefreshToken(String token) {
        String key = REFRESH_PREFIX + hashToken(token);
        redisClient.del(key);
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash refresh token", exception);
        }
    }
}
