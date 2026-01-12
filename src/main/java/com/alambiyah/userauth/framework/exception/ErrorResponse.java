package com.alambiyah.userauth.framework.exception;

public record ErrorResponse(
        String message,
        Object details
) {
}
