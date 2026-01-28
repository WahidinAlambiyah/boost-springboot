package com.example.boost.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String issuer;
    private String secret;
    private long accessTokenTtlMinutes;
    private long refreshTokenTtlDays;
}
