package com.alambiyah.userauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(min = 3, max = 255) String usernameOrEmail,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
