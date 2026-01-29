package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDisableRequest {
    @NotBlank
    private String reason;
}
