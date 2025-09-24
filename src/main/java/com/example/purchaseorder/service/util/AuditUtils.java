package com.example.purchaseorder.service.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;

public final class AuditUtils {

    private static final String SYSTEM_USER = "SYSTEM";
    private static final String ANONYMOUS_USER = "anonymousUser";

    private AuditUtils() {
    }

    public static String resolveCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return SYSTEM_USER;
        }
        String name = authentication.getName();
        if (name == null || name.isBlank() || ANONYMOUS_USER.equalsIgnoreCase(name)) {
            return SYSTEM_USER;
        }
        return name;
    }

    public static OffsetDateTime currentDateTime() {
        return OffsetDateTime.now();
    }
}
