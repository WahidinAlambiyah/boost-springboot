package com.example.purchaseorder.security;

public final class SecurityRoles {

    private SecurityRoles() {
    }

    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    public static final String HAS_ROLE_ADMIN = "hasRole('" + ADMIN + "')";
    public static final String HAS_ANY_ROLE_ADMIN_OR_USER = "hasAnyRole('" + ADMIN + "','" + USER + "')";
}
