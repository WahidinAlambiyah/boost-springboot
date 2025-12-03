package com.example.boost.service;

import com.example.boost.domain.PasswordResetToken;
import com.example.boost.domain.User;
import com.example.boost.dto.ResetPasswordRequest;
import com.example.boost.repository.PasswordResetTokenRepository;
import com.example.boost.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final Duration DEFAULT_EXPIRY = Duration.ofMinutes(60);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SessionService sessionService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset.frontend-url:${app.base-url.frontend:https://protofeone.alambiyah.com}}")
    private String frontendUrl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService,
                                SessionService sessionService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.sessionService = sessionService;
    }

    @Transactional
    public void requestReset(String email, String requestIp, String userAgent) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.info("Password reset requested for non-existing email {}", email);
            return;
        }

        User user = userOpt.get();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(generateToken());
        token.setExpiredAt(Instant.now().plus(DEFAULT_EXPIRY));
        token.setRequestIp(requestIp);
        token.setUserAgent(userAgent);
        passwordResetTokenRepository.save(token);

        String resetLink = String.format("%s/reset-password?token=%s", frontendUrl, token.getToken());
        sendResetEmail(user.getEmail(), resetLink);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Link reset tidak valid atau sudah kadaluarsa."));

        if (token.getUsedAt() != null || token.getExpiredAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link reset tidak valid atau sudah kadaluarsa.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Konfirmasi password tidak sesuai.");
        }

        PasswordValidator.validatePasswordRules(request.getNewPassword());

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setPasswordChangedAt(Instant.now());
        token.setUsedAt(Instant.now());
        userRepository.save(user);
        passwordResetTokenRepository.save(token);
        sessionService.invalidateUserSessions(user.getId());
    }

    private void sendResetEmail(String to, String resetLink) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    trySendEmail(to, resetLink);
                }
            });
        } else {
            trySendEmail(to, resetLink);
        }
    }

    private void trySendEmail(String to, String resetLink) {
        try {
            emailService.sendEmail(to, "Reset Password", "Gunakan tautan berikut untuk reset password: " + resetLink);
        } catch (ResponseStatusException ex) {
            log.error("Gagal mengirim email reset password untuk {}: {}", to, ex.getReason());
        }
    }

    private String generateToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return UUID.randomUUID() + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
