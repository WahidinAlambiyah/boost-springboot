package com.example.boost.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username:}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }

        try {
            mailSender.send(message);
            log.info("Email sent to {} with subject '{}'", to, subject);
        } catch (MailAuthenticationException ex) {
            log.error("Failed to send email due to authentication error", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Gagal mengirim email: kredensial SMTP tidak valid atau ditolak.");
        } catch (MailException ex) {
            log.error("Failed to send email", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Gagal mengirim email: layanan email tidak tersedia.");
        }
    }
}
