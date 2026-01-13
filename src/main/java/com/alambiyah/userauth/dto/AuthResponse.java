package com.alambiyah.userauth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
