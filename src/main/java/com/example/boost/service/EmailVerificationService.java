package com.example.boost.service;

import com.example.boost.domain.EmailVerification;
import com.example.boost.domain.User;
import com.example.boost.repository.EmailVerificationRepository;
import com.example.boost.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private static final Duration DEFAULT_EXPIRY = Duration.ofHours(2);

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);

    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.verification.base-url:https://app-kamu.com}")
    private String baseUrl;

    public EmailVerificationService(EmailVerificationRepository emailVerificationRepository,
                                    EmailService emailService,
                                    UserRepository userRepository) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void sendVerification(User user) {
        EmailVerification verification = new EmailVerification();
        verification.setUser(user);
        verification.setExpiredAt(Instant.now().plus(DEFAULT_EXPIRY));
        verification.setToken(generateToken());
        emailVerificationRepository.save(verification);

        String verificationLink = String.format("%s/verify-email?token=%s", baseUrl, verification.getToken());
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        emailService.sendEmail(user.getEmail(), "Verifikasi Email",
                                "Silakan verifikasi akun Anda melalui tautan: " + verificationLink);
                    } catch (ResponseStatusException ex) {
                        log.error("Gagal mengirim email verifikasi untuk {}: {}", user.getEmail(), ex.getReason());
                    }
                }
            });
        } else {
            try {
                emailService.sendEmail(user.getEmail(), "Verifikasi Email",
                        "Silakan verifikasi akun Anda melalui tautan: " + verificationLink);
            } catch (ResponseStatusException ex) {
                log.error("Gagal mengirim email verifikasi untuk {}: {}", user.getEmail(), ex.getReason());
            }
        }
    }

    @Transactional
    public void verify(String token) {
        EmailVerification verification = emailVerificationRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token verifikasi tidak ditemukan."));

        if (verification.getUsedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token verifikasi sudah digunakan. Silakan minta tautan baru.");
        }
        if (verification.getExpiredAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token verifikasi sudah kedaluwarsa. Silakan minta tautan baru.");
        }

        User user = verification.getUser();
        user.setActive(true);
        verification.setUsedAt(Instant.now());
        userRepository.save(user);
        emailVerificationRepository.save(verification);
    }

    private String generateToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return UUID.randomUUID() + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
