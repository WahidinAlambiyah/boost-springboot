package com.alambiyah.app;

public record User(
        String id,
        String username,
        String email,
        String fullName
) {
}
