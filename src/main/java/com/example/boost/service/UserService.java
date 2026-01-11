package com.example.boost.service;

import com.example.boost.domain.dto.CreateUserRequest;
import com.example.boost.domain.dto.UpdateUserRequest;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.User;
import com.example.boost.exception.BadRequestException;
import com.example.boost.exception.NotFoundException;
import com.example.boost.repository.UserRepository;
import com.example.boost.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(request.getRoles());
        user.setActive(request.isActive());
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, UpdateUserRequest request) {
        User user = getById(id);
        if (request.getUsername() != null && !request.getUsername().equalsIgnoreCase(user.getUsername())) {
            if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
                throw new BadRequestException("Username already exists");
            }
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setPasswordChangedAt(OffsetDateTime.now());
        }
        if (request.getRoles() != null) {
            user.setRoles(request.getRoles());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void softDeleteUser(UUID id) {
        User user = getById(id);
        user.setDeletedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new NotFoundException("User not found");
        }
        return principal.getUser();
    }
}
