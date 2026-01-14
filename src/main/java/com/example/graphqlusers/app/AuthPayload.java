package com.example.graphqlusers.app;

public record AuthPayload(
        String accessToken,
        String refreshToken,
        User user
) {
}
