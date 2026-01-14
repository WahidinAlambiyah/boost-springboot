package com.example.graphqlusers.app;

import java.util.Set;

public record CreateUserInput(
        String username,
        String email,
        String fullName,
        String password,
        Set<Role> roles
) {
}
