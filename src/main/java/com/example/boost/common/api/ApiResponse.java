package com.example.boost.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "ApiResponse")
public class ApiResponse<T> {

    @Schema(example = "200")
    private int status;

    @Schema(example = "Logged in")
    private String message;

    @Schema(nullable = true)
    private T data;

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Object> error(int status, String message) {
        return ApiResponse.builder()
                .status(status)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }
}
