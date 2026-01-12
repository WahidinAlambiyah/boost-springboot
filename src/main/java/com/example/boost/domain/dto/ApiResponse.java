package com.example.boost.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ApiResponse<T> {
    private OffsetDateTime timestamp;
    private int status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(OffsetDateTime.now())
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Object> error(int status, String message) {
        return ApiResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(status)
                .message(message)
                .build();
    }
}
