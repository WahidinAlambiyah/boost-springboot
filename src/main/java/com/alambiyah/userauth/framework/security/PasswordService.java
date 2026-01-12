package com.alambiyah.userauth.framework.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordService {
    public String hash(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }

    public boolean matches(String plainPassword, String hashedPassword) {
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}
