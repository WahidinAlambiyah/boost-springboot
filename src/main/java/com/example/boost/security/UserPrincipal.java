package com.example.boost.security;

import com.example.boost.domain.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UserPrincipal implements UserDetails {
    @Getter
    private final User user;
    private final Set<GrantedAuthority> authorities;

    public UserPrincipal(User user, Collection<String> roleCodes, Collection<String> permissionCodes) {
        this.user = user;
        Set<GrantedAuthority> granted = new HashSet<>();
        if (roleCodes != null) {
            for (String role : roleCodes) {
                granted.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }
        if (permissionCodes != null) {
            for (String permission : permissionCodes) {
                granted.add(new SimpleGrantedAuthority(permission));
            }
        }
        this.authorities = Set.copyOf(granted);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
