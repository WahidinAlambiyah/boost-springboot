package com.alambiyah.userauth.framework.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.alambiyah.userauth.domain.entity.User;

import io.quarkus.redis.client.RedisClient;
import io.smallrye.jwt.build.Jwt;
import io.vertx.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

    @ConfigProperty(name = "app.redis.enabled", defaultValue = "true")
    boolean redisEnabled;

    private final Map<String, TokenEntry> refreshTokenStore = new ConcurrentHashMap<>();

    @Inject
    public TokenService(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTokenTtl);

        Set<String> groups = (user.roles == null)
                ? Set.of()
                : user.roles.stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet());

        return Jwt.subject(user.id.toString())
                .preferredUserName(user.username)
                .issuer(issuer)
                .groups(groups) // ✅ Set<String>
                .issuedAt(now)
                .expiresAt(exp)
                .sign();
    }

    public String generateRefreshToken(User user) {
        String token = generateOpaqueToken();
        String key = REFRESH_PREFIX + hashToken(token);
        if (redisEnabled) {
            redisClient.setex(key, String.valueOf(refreshTokenTtl.toSeconds()), user.id.toString());
        } else {
            refreshTokenStore.put(key, new TokenEntry(user.id.toString(), Instant.now().plus(refreshTokenTtl)));
        }
        return token;
    }

    public Optional<String> consumeRefreshToken(String token) {
        String key = REFRESH_PREFIX + hashToken(token);
        if (redisEnabled) {
            Response response = redisClient.get(key);
            if (response == null) {
                return Optional.empty();
            }
            String userId = response.toString();
            redisClient.del(List.of(key));
            return Optional.of(userId);
        }
        TokenEntry entry = refreshTokenStore.remove(key);
        if (entry == null || entry.isExpired()) {
            return Optional.empty();
        }
        return Optional.of(entry.userId());
    }

    public void revokeRefreshToken(String token) {
        String key = REFRESH_PREFIX + hashToken(token);
        if (redisEnabled) {
            redisClient.del(List.of(key));
        } else {
            refreshTokenStore.remove(key);
        }
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

    private record TokenEntry(String userId, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
