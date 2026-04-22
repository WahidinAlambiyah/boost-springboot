package com.example.boost.controller;

import com.example.boost.domain.dto.PasswordResetConfirmRequest;
import com.example.boost.domain.dto.PasswordResetRequestDto;
import com.example.boost.domain.dto.PasswordResetResendRequest;
import com.example.boost.exception.BadRequestException;
import com.example.boost.service.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "2. Password Management", description = "Password reset & recovery")
@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<Void> requestReset(@Valid @RequestBody PasswordResetRequestDto request) {
        passwordResetService.requestReset(request.getEmail(), request.getMode());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend")
    public ResponseEntity<Void> resend(@Valid @RequestBody PasswordResetResendRequest request) {
        passwordResetService.resendOtp(request.getResetRequestId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@Valid @RequestBody PasswordResetConfirmRequest request) {
        boolean hasOtpFlow = request.getResetRequestId() != null && request.getOtp() != null;
        boolean hasTokenFlow = request.getToken() != null;
        if (hasOtpFlow) {
            passwordResetService.confirmWithOtp(request.getResetRequestId(), request.getOtp(), request.getNewPassword());
        } else if (hasTokenFlow) {
            passwordResetService.confirmWithToken(request.getToken(), request.getNewPassword());
        } else {
            throw new BadRequestException("Invalid password reset payload");
        }
        return ResponseEntity.noContent().build();
    }
}
