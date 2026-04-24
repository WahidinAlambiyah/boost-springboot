package com.example.boost.common.exception;

public class TooManyRequestsException extends AppException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
