package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcademyLocationUpdateRequest {
    @NotBlank
    @Size(min = 3, max = 255)
    private String name;

    @Size(max = 2_000)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String country;

    @Size(max = 100)
    private String timezone;

    @Size(max = 30)
    private String phone;
}
