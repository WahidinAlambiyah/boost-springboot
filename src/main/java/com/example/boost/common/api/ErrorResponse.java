package com.example.boost.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(example = "400")
    public int status;

    @Schema(example = "Bad Request")
    public String error;

    @Schema(example = "Validation failed")
    public String message;

    @Schema(example = "/api/users")
    public String path;

    @Schema(example = "2026-04-17T10:00:00")
    public String timestamp;
}
