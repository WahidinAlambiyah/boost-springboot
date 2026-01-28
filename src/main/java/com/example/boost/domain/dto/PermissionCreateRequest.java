package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionCreateRequest {
    @NotBlank
    @Pattern(regexp = "^[A-Z0-9_]+$")
    private String code;

    @NotBlank
    private String name;

    private String module;

    private String description;

    private Boolean isActive;
}
