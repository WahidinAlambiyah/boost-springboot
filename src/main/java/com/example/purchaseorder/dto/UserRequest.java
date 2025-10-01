package com.example.purchaseorder.dto;

import com.example.purchaseorder.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequest {

    private static final String PHONE_PATTERN = "^\\+?[0-9]{8,15}$";

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email(message = "Email must be valid.")
    private String email;

    @NotBlank
    @Pattern(regexp = PHONE_PATTERN, message = "Phone number must contain 8 to 15 digits and may start with '+'.")
    private String phone;

    @NotBlank
    private String password;

    @NotNull
    private UserRole role;
}
