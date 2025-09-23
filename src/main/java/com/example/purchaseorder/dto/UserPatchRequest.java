package com.example.purchaseorder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserPatchRequest {

    private static final String PHONE_PATTERN = "^\\+?[0-9]{8,15}$";
    private static final String NOT_BLANK_REGEX = "^(?!\\s*$).+";

    @Pattern(regexp = NOT_BLANK_REGEX, message = "First name must not be blank.")
    private String firstName;

    @Pattern(regexp = NOT_BLANK_REGEX, message = "Last name must not be blank.")
    private String lastName;

    @Email(message = "Email must be valid.")
    @Pattern(regexp = NOT_BLANK_REGEX, message = "Email must not be blank.")
    private String email;

    @Pattern(regexp = PHONE_PATTERN, message = "Phone number must contain 8 to 15 digits and may start with '+'.")
    private String phone;

    @Pattern(regexp = NOT_BLANK_REGEX, message = "Password must not be blank.")
    private String password;
}
