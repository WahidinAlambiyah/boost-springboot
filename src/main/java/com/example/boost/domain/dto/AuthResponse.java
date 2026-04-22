package com.example.boost.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AuthResponse")
public class AuthResponse {

    @Schema(example = "xxx")
    private String accessToken;

    @Schema(example = "xxx")
    private String refreshToken;

    @Schema(example = "Bearer")
    private String tokenType;
}
