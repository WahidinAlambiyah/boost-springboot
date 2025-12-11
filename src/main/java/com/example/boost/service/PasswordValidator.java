package com.example.boost.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class PasswordValidator {

    private PasswordValidator() {
    }

    static void validatePasswordRules(String password) {
        boolean meetsComplexity = password.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}");
        boolean isPassphrase = password.length() >= 15 && password.contains(" ");
        if (!meetsComplexity && !isPassphrase) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password harus kombinasi huruf besar, huruf kecil, angka, dan simbol atau passphrase minimal 15 karakter.");
        }
    }
}
