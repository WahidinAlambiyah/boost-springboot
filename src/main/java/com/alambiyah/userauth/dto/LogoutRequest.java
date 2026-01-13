package com.alambiyah.userauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogoutRequest(
        @NotBlank @Size(min = 20, max = 500) String refreshToken
) {
}
