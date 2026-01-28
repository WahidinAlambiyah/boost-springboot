package com.example.boost.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {
    private int status;
    private String message;
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
}
