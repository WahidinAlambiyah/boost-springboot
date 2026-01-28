package com.example.boost.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {
    private boolean enabled = true;
    private MethodSecurity methodSecurity = new MethodSecurity();

    @Getter
    @Setter
    public static class MethodSecurity {
        private boolean enabled = true;
    }
}
