package com.example.boost.security;

import com.example.boost.domain.entity.User;
import com.example.boost.iam.infrastructure.PermissionRepository;
import com.example.boost.iam.infrastructure.RoleRepository;
import com.example.boost.iam.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<String> roles = roleRepository.findCodesByUserId(user.getId());
        List<String> permissions = permissionRepository.findCodesByUserId(user.getId());
        return new UserPrincipal(user, roles, permissions);
    }
}
