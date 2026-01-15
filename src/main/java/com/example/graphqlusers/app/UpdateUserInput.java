package com.example.graphqlusers.app;

import java.util.Set;

public record UpdateUserInput(
        String email,
        String fullName,
        String password,
        Set<Role> roles
) {
}
