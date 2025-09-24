package com.example.purchaseorder.service.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuditUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolveCurrentAuditorShouldReturnSystemWhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        String auditor = AuditUtils.resolveCurrentAuditor();

        assertThat(auditor).isEqualTo("SYSTEM");
    }

    @Test
    void resolveCurrentAuditorShouldReturnSystemWhenNotAuthenticated() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("tester", "pw");
        authentication.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String auditor = AuditUtils.resolveCurrentAuditor();

        assertThat(auditor).isEqualTo("SYSTEM");
    }

    @Test
    void resolveCurrentAuditorShouldReturnSystemForAnonymousOrBlankUsers() {
        TestingAuthenticationToken anonymous = new TestingAuthenticationToken("anonymousUser", "pw");
        SecurityContextHolder.getContext().setAuthentication(anonymous);
        assertThat(AuditUtils.resolveCurrentAuditor()).isEqualTo("SYSTEM");

        TestingAuthenticationToken blankName = new TestingAuthenticationToken("   ", "pw");
        SecurityContextHolder.getContext().setAuthentication(blankName);
        assertThat(AuditUtils.resolveCurrentAuditor()).isEqualTo("SYSTEM");
    }

    @Test
    void resolveCurrentAuditorShouldReturnAuthenticatedName() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("auditor", "pw");
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String auditor = AuditUtils.resolveCurrentAuditor();

        assertThat(auditor).isEqualTo("auditor");
    }

    @Test
    void currentDateTimeShouldReturnCurrentTimestamp() {
        OffsetDateTime before = OffsetDateTime.now();

        OffsetDateTime result = AuditUtils.currentDateTime();

        OffsetDateTime after = OffsetDateTime.now();
        assertThat(Duration.between(before, result)).isBetween(Duration.ZERO, Duration.ofSeconds(1));
        assertThat(Duration.between(result, after)).isBetween(Duration.ZERO, Duration.ofSeconds(1));
    }
}
