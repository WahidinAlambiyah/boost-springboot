package com.example.boost.common.exception;

/**
 * Base exception for application-level errors that should be translated into a controlled HTTP response.
 */
public abstract class AppException extends RuntimeException {
    protected AppException(String message) {
        super(message);
    }
}
