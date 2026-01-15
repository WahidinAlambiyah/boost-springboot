package com.example.graphqlusers.app;

public record User(
        String id,
        String username,
        String email,
        String fullName
) {
}
