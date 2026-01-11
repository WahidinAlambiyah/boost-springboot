package com.example.boost.domain.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
