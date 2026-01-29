package com.example.boost.controller;

import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.domain.dto.PasswordResetConfirmRequest;
import com.example.boost.domain.dto.PasswordResetRequestDto;
import com.example.boost.domain.dto.PasswordResetResendRequest;
import com.example.boost.exception.BadRequestException;
import com.example.boost.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Object>> requestReset(@Valid @RequestBody PasswordResetRequestDto request) {
        passwordResetService.requestReset(request.getEmail(), request.getMode());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "If the email exists, reset instructions were sent", null));
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Object>> resend(@Valid @RequestBody PasswordResetResendRequest request) {
        passwordResetService.resendOtp(request.getResetRequestId());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "OTP resent", null));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Object>> confirm(@Valid @RequestBody PasswordResetConfirmRequest request) {
        boolean hasOtpFlow = request.getResetRequestId() != null && request.getOtp() != null;
        boolean hasTokenFlow = request.getToken() != null;
        if (hasOtpFlow) {
            passwordResetService.confirmWithOtp(request.getResetRequestId(), request.getOtp(), request.getNewPassword());
        } else if (hasTokenFlow) {
            passwordResetService.confirmWithToken(request.getToken(), request.getNewPassword());
        } else {
            throw new BadRequestException("Invalid password reset payload");
        }
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Password reset successful", null));
    }
}
