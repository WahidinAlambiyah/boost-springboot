package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPasswordUpdateRequest {
    @NotBlank
    @Size(min = 8, max = 72)
    private String newPassword;
}
