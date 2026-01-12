package com.example.boost.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String issuer;
    private String secret;
    private long accessTokenTtlMinutes;
    private long refreshTokenTtlDays;
}
