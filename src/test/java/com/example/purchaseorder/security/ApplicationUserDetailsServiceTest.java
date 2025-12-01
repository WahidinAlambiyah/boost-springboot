package com.example.purchaseorder.security;

import com.example.purchaseorder.domain.User;
import com.example.purchaseorder.domain.UserRole;
import com.example.purchaseorder.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationUserDetailsService service;

    @Test
    void loadUserByUsernameShouldReturnAuthoritiesBasedOnRole() {
        User admin = User.builder()
                .email("admin@example.com")
                .password("secret")
                .role(UserRole.ADMIN)
                .build();

        when(userRepository.findByEmailAndDeletedFalse("admin@example.com")).thenReturn(Optional.of(admin));

        UserDetails userDetails = service.loadUserByUsername("admin@example.com");

        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsernameShouldDefaultToUserRoleWhenNull() {
        User user = User.builder()
                .email("user@example.com")
                .password("secret")
                .build();
        user.setRole(null);

        when(userRepository.findByEmailAndDeletedFalse("user@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = service.loadUserByUsername("user@example.com");

        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUserNotFound() {
        when(userRepository.findByEmailAndDeletedFalse("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@example.com");
    }
}
