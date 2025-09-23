package com.example.purchaseorder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = PHONE_PATTERN, message = "Phone number must contain 8 to 15 digits and may start with '+'.")
    private String phone;

    @NotBlank
    private String password;
}
