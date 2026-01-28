package com.example.boost.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserRolesUpdateRequest {
    @NotEmpty
    private Set<String> roleCodes;
}
