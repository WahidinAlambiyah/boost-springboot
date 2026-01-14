package com.example.graphqlusers.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class PasswordHasher {
    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        return BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
    }

    public static boolean verify(String rawPassword, String hash) {
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), hash).verified;
    }
}
