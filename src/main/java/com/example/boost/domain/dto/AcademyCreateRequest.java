package com.example.boost.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcademyCreateRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String code;

    @NotBlank
    @Size(min = 3, max = 255)
    private String name;

    @Size(max = 2_000)
    private String description;

    @Size(max = 30)
    private String phone;

    @Email
    @Size(max = 255)
    private String email;
}
