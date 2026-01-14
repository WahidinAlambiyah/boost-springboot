package com.example.graphqlusers.app;

public record RegisterInput(
        String username,
        String email,
        String fullName,
        String password
) {
}
