package com.example.boost.domain.dto;

import com.example.boost.domain.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    @Size(min = 3, max = 50)
    private String username;

    @Email
    @Size(max = 255)
    private String email;

    @Size(min = 8, max = 100)
    private String password;

    @Size(max = 255)
    private String fullName;

    @Size(max = 30)
    private String phoneNumber;

    private Set<Role> roles;

    private Boolean active;
}
