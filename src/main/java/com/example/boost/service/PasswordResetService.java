package com.example.boost.service;

import com.example.boost.context.RequestContext;
import com.example.boost.context.RequestContextData;
import com.example.boost.domain.entity.PasswordResetRequest;
import com.example.boost.domain.entity.PasswordResetStatus;
import com.example.boost.domain.entity.User;
import com.example.boost.exception.BadRequestException;
import com.example.boost.exception.TooManyRequestsException;
import com.example.boost.repository.PasswordResetRequestRepository;
import com.example.boost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(10);
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_REQUESTS = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(10);

    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final AuditLogService auditLogService;
    private final TokenService tokenService;

    private final ConcurrentHashMap<String, RateWindow> ipRateLimits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateWindow> emailRateLimits = new ConcurrentHashMap<>();

    @Transactional
    public void requestReset(String email, String mode) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String ip = resolveIp();
        if (isRateLimited(ipRateLimits, ip) || isRateLimited(emailRateLimits, normalizedEmail)) {
            auditLogService.securityEvent("PASSWORD_RESET_RATE_LIMITED")
                    .statusFailure("Rate limit exceeded")
                    .metadata(Map.of("email", maskEmail(normalizedEmail)))
                    .save();
            throw new TooManyRequestsException("Too many reset requests. Please try again later.");
        }

        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(normalizedEmail);
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setEmail(normalizedEmail);
        resetRequest.setStatus(PasswordResetStatus.PENDING);
        resetRequest.setAttemptCount(0);
        resetRequest.setExpiresAt(OffsetDateTime.now().plus(OTP_EXPIRY));
        resetRequest.setRequestedIp(ip);
        resetRequest.setRequestedUserAgent(resolveUserAgent());
        userOptional.ifPresent(user -> resetRequest.setUserId(user.getId()));

        auditLogService.securityEvent("PASSWORD_RESET_REQUESTED")
                .statusSuccess()
                .metadata(Map.of("resetRequestId", resetRequest.getId(), "email", maskEmail(normalizedEmail)))
                .save();

        if ("OTP".equalsIgnoreCase(mode)) {
            String otp = generateOtp();
            resetRequest.setOtpHash(hashValue(otp));
            resetRequest.setOtpLastSentAt(OffsetDateTime.now());
            passwordResetRequestRepository.save(resetRequest);
            if (userOptional.isPresent()) {
                mailService.sendPasswordResetOtp(normalizedEmail, otp);
            }
            auditLogService.securityEvent("PASSWORD_RESET_OTP_SENT")
                    .statusSuccess()
                    .metadata(Map.of("resetRequestId", resetRequest.getId(), "email", maskEmail(normalizedEmail)))
                    .save();
        } else {
            String token = UUID.randomUUID().toString();
            resetRequest.setTokenHash(hashValue(token));
            passwordResetRequestRepository.save(resetRequest);
            if (userOptional.isPresent()) {
                mailService.sendPasswordResetToken(normalizedEmail,
                        "https://example.com/reset?token=" + token);
            }
        }
    }

    @Transactional
    public void resendOtp(UUID resetRequestId) {
        PasswordResetRequest resetRequest = passwordResetRequestRepository.findById(resetRequestId)
                .orElseThrow(() -> new BadRequestException("Invalid reset request"));
        if (resetRequest.getStatus() != PasswordResetStatus.PENDING) {
            throw new BadRequestException("Reset request is not active");
        }
        if (resetRequest.getOtpHash() == null) {
            throw new BadRequestException("OTP not available for this request");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (resetRequest.getOtpLastSentAt() != null
                && resetRequest.getOtpLastSentAt().plus(RESEND_COOLDOWN).isAfter(now)) {
            throw new BadRequestException("Please wait before resending OTP");
        }
        String otp = generateOtp();
        resetRequest.setOtpHash(hashValue(otp));
        resetRequest.setOtpLastSentAt(now);
        resetRequest.setExpiresAt(now.plus(OTP_EXPIRY));
        passwordResetRequestRepository.save(resetRequest);
        mailService.sendPasswordResetOtp(resetRequest.getEmail(), otp);
        auditLogService.securityEvent("PASSWORD_RESET_OTP_SENT")
                .statusSuccess()
                .metadata(Map.of("resetRequestId", resetRequest.getId(), "email", maskEmail(resetRequest.getEmail())))
                .save();
    }

    @Transactional
    public void confirmWithOtp(UUID resetRequestId, String otp, String newPassword) {
        PasswordResetRequest resetRequest = passwordResetRequestRepository.findById(resetRequestId)
                .orElseThrow(() -> new BadRequestException("Invalid reset request"));
        validatePending(resetRequest);
        if (resetRequest.getOtpHash() == null) {
            throw new BadRequestException("OTP not available");
        }
        if (!hashValue(otp).equals(resetRequest.getOtpHash())) {
            registerFailedAttempt(resetRequest, "Invalid OTP");
            throw new BadRequestException("Invalid OTP");
        }
        completeReset(resetRequest, newPassword);
    }

    @Transactional
    public void confirmWithToken(String token, String newPassword) {
        String tokenHash = hashValue(token);
        PasswordResetRequest resetRequest = passwordResetRequestRepository
                .findByTokenHashAndStatus(tokenHash, PasswordResetStatus.PENDING)
                .orElseThrow(() -> {
                    auditLogService.securityEvent("PASSWORD_RESET_CONFIRM_FAILED")
                            .statusFailure("Invalid reset token")
                            .metadata(Map.of("tokenHash", tokenHash))
                            .save();
                    return new BadRequestException("Invalid reset token");
                });
        validatePending(resetRequest);
        completeReset(resetRequest, newPassword);
    }

    private void validatePending(PasswordResetRequest resetRequest) {
        if (resetRequest.getStatus() != PasswordResetStatus.PENDING) {
            throw new BadRequestException("Reset request is not active");
        }
        if (resetRequest.getExpiresAt().isBefore(OffsetDateTime.now())) {
            resetRequest.setStatus(PasswordResetStatus.EXPIRED);
            passwordResetRequestRepository.save(resetRequest);
            throw new BadRequestException("Reset request expired");
        }
    }

    private void registerFailedAttempt(PasswordResetRequest resetRequest, String reason) {
        resetRequest.setAttemptCount(resetRequest.getAttemptCount() + 1);
        if (resetRequest.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
            resetRequest.setStatus(PasswordResetStatus.EXPIRED);
        }
        passwordResetRequestRepository.save(resetRequest);
        auditLogService.securityEvent("PASSWORD_RESET_CONFIRM_FAILED")
                .statusFailure(reason)
                .metadata(Map.of("resetRequestId", resetRequest.getId(), "email", maskEmail(resetRequest.getEmail())))
                .save();
    }

    private void completeReset(PasswordResetRequest resetRequest, String newPassword) {
        User user = userRepository.findByEmailIgnoreCase(resetRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(OffsetDateTime.now());
        userRepository.save(user);
        tokenService.revokeAllRefreshTokens(user.getId().toString());

        resetRequest.setStatus(PasswordResetStatus.USED);
        resetRequest.setUsedAt(OffsetDateTime.now());
        resetRequest.setConfirmedIp(resolveIp());
        passwordResetRequestRepository.save(resetRequest);

        auditLogService.securityEvent("PASSWORD_RESET_CONFIRM_SUCCESS")
                .statusSuccess()
                .metadata(Map.of("resetRequestId", resetRequest.getId(), "email", maskEmail(resetRequest.getEmail())))
                .save();
    }

    private boolean isRateLimited(ConcurrentHashMap<String, RateWindow> map, String key) {
        long now = System.currentTimeMillis();
        RateWindow window = map.computeIfAbsent(key, ignore -> new RateWindow(now));
        synchronized (window) {
            if (now - window.startMillis >= RATE_LIMIT_WINDOW.toMillis()) {
                window.startMillis = now;
                window.count = 0;
            }
            window.count++;
            return window.count > MAX_REQUESTS;
        }
    }

    private String generateOtp() {
        int number = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(number);
    }

    private String hashValue(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Hashing algorithm unavailable", ex);
        }
    }

    private String resolveIp() {
        RequestContextData contextData = RequestContext.get();
        if (contextData != null && contextData.getIp() != null) {
            return contextData.getIp();
        }
        return "unknown";
    }

    private String resolveUserAgent() {
        RequestContextData contextData = RequestContext.get();
        if (contextData != null && contextData.getUserAgent() != null) {
            return contextData.getUserAgent();
        }
        return "unknown";
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private static final class RateWindow {
        private long startMillis;
        private int count;

        private RateWindow(long startMillis) {
            this.startMillis = startMillis;
        }
    }
}
