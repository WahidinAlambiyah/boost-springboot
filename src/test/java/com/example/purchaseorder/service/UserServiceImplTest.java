package com.example.purchaseorder.service;

import com.example.purchaseorder.domain.User;
import com.example.purchaseorder.dto.UserRequest;
import com.example.purchaseorder.exception.ResourceNotFoundException;
import com.example.purchaseorder.repository.UserRepository;
import com.example.purchaseorder.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest request;

    @BeforeEach
    void setUp() {
        request = new UserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhone("123456789");
        request.setPassword("password");
        request.setCreatedBy("tester");
        request.setCreatedDatetime(OffsetDateTime.now());
        request.setUpdatedBy("tester");
        request.setUpdatedDatetime(OffsetDateTime.now());
    }

    @Test
    void createShouldEncodePasswordAndSave() {
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.create(request);

        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getPassword()).isEqualTo("encoded");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void updateShouldModifyExistingUser() {
        User existing = User.builder().id(1L).email("john.doe@example.com").password("old").build();
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        User updated = userService.update(1L, request);

        assertThat(updated.getPassword()).isEqualTo("encoded");
        verify(userRepository).save(existing);
    }

    @Test
    void updateShouldThrowWhenNotFound() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteShouldSoftDeleteUser() {
        User existing = User.builder().id(1L).build();
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        userService.delete(1L);

        assertThat(existing.isDeleted()).isTrue();
        verify(userRepository).save(existing);
    }

    @Test
    void deletePermanentShouldRemoveUser() {
        User existing = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        userService.deletePermanent(1L);

        verify(userRepository).delete(existing);
    }

    @Test
    void getShouldReturnUser() {
        User existing = User.builder().id(1L).build();
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));

        assertThat(userService.get(1L)).isEqualTo(existing);
    }

    @Test
    void listShouldReturnAllUsers() {
        List<User> users = List.of(User.builder().id(1L).build());
        Page<User> page = new PageImpl<>(users);
        PageRequest pageable = PageRequest.of(0, 10);
        when(userRepository.findAllByDeletedFalse(pageable)).thenReturn(page);

        Page<User> result = userService.list(pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findAllByDeletedFalse(pageable);
    }
}
