package com.example.boost.service;

import com.example.boost.domain.dto.UserCreateRequest;
import com.example.boost.domain.dto.UserUpdateRequest;
import com.example.boost.domain.entity.AuditLog;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.User;
import com.example.boost.exception.NotFoundException;
import com.example.boost.repository.RoleRepository;
import com.example.boost.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUpAuditLog() {
        AuditLogService.AuditLogBuilder builder = Mockito.mock(
                AuditLogService.AuditLogBuilder.class,
                Mockito.RETURNS_SELF
        );
        Mockito.lenient().when(builder.save()).thenReturn(new AuditLog());
        Mockito.lenient().when(auditLogService.securityEvent(any())).thenReturn(builder);
        Mockito.lenient().when(auditLogService.rbacEvent(any())).thenReturn(builder);
        Mockito.lenient().when(auditLogService.dataEvent(any())).thenReturn(builder);
    }

    @Test
    void createUserHashesPassword() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("demo");
        request.setEmail("demo@example.com");
        request.setPassword("Password123!");
        request.setRoleCodes(Set.of("USER"));

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setCode("USER");
        when(roleRepository.findByCodeIn(Set.of("USER"))).thenReturn(List.of(role));
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed");

        userService.createUser(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findWithRolesById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(id, new UserUpdateRequest()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUsersUsesRepositoryPaging() {
        when(userRepository.findAll(any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        userService.getUsers(PageRequest.of(0, 20), null);
        verify(userRepository).findAll(any(), any(PageRequest.class));
    }

    @Test
    void disableUserSetsFlags() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setActive(true);
        when(userRepository.findWithRolesById(user.getId())).thenReturn(Optional.of(user));

        userService.disableUser(user.getId(), "policy");

        assertThat(user.isActive()).isFalse();
        assertThat(user.getDisabledAt()).isNotNull();
    }

    @Test
    void unlockUserResetsLockout() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFailedLoginCount(5);
        user.setLockedUntil(java.time.OffsetDateTime.now().plusMinutes(5));
        when(userRepository.findWithRolesById(user.getId())).thenReturn(Optional.of(user));

        userService.unlockUser(user.getId());

        assertThat(user.getFailedLoginCount()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }
}
