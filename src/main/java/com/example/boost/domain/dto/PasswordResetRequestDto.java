package com.example.boost.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequestDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "OTP|TOKEN")
    private String mode;
}
