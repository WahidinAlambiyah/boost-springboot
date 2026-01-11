package com.example.boost.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security.login")
public class LoginProperties {
    private int maxFailedAttempts;
    private int lockMinutes;
}
