package com.alambiyah.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 120) String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 3, max = 255) String fullName,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
