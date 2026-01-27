package com.example.boost.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service("authz")
@RequiredArgsConstructor
public class Authz {

    private final AppSecurityProperties props;

    /**
     * Return true if security is disabled, otherwise checks if current user has the given authority.
     * Example: @PreAuthorize("@authz.has('USER_READ')")
     */
    public boolean has(String authority) {
        if (!props.enabled()) return true; // ✅ bypass all checks when security off

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;

        return auth.getAuthorities().stream()
                .filter(Objects::nonNull)
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    /**
     * Return true if security is disabled, otherwise checks if current user has ANY of the given authorities.
     * Example: @PreAuthorize("@authz.any('USER_WRITE','USER_DELETE')")
     */
    public boolean any(String... authorities) {
        if (!props.enabled()) return true;

        if (authorities == null || authorities.length == 0) return false;
        Set<String> required = Arrays.stream(authorities).filter(Objects::nonNull).collect(Collectors.toSet());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;

        return auth.getAuthorities().stream()
                .filter(Objects::nonNull)
                .map(GrantedAuthority::getAuthority)
                .anyMatch(required::contains);
    }

    /**
     * Optional helper: checks roles in format ROLE_XXX.
     * Example: @PreAuthorize("@authz.hasRole('ADMIN')")
     */
    public boolean hasRole(String role) {
        if (!props.enabled()) return true;
        if (role == null || role.isBlank()) return false;
        return has("ROLE_" + role);
    }
}
