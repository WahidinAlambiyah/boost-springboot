package com.example.boost.domain.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    private String fullName;

    @Email
    private String email;

    private Boolean isActive;
}
