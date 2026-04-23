package com.example.boost.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class RegisterResponse {
    private String username;
    private String email;
    private Set<String> roleCodes;
}
