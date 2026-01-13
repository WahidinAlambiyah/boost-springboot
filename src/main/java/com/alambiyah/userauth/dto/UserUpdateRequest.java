package com.alambiyah.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UserUpdateRequest(
        @Size(min = 3, max = 120) String username,
        @Email @Size(max = 255) String email,
        @Size(min = 3, max = 255) String fullName,
        @Size(min = 8, max = 100) String password,
        Set<@Size(min = 3, max = 50) String> roles,
        Boolean isActive
) {
}
