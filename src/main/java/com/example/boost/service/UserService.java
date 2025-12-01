package com.example.boost.service;

import com.example.boost.domain.Role;
import com.example.boost.domain.User;
import com.example.boost.dto.RegisterUserRequest;
import com.example.boost.dto.UpdateUserRequest;
import com.example.boost.dto.UserResponse;
import com.example.boost.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        Set<Role> roles = roleService.resolveRoles(request.getRoles());
        user.setRoles(roles);
        user.setActive(true);
        return userRepository.save(user);
    }

    public UserResponse createUser(RegisterUserRequest request) {
        User saved = register(request);
        return mapToResponse(saved);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<UserResponse> getUser(UUID id) {
        return userRepository.findById(id).map(this::mapToResponse);
    }

    @Transactional
    public Optional<UserResponse> updateUser(UUID id, UpdateUserRequest request) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setActive(request.isActive());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }
            Set<Role> roles = roleService.resolveRoles(request.getRoles());
            user.setRoles(roles);
            return mapToResponse(user);
        });
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public Optional<UserResponse> assignRoles(UUID userId, Set<String> roleNames) {
        return userRepository.findById(userId).map(user -> {
            Set<Role> roles = roleService.resolveRoles(roleNames);
            user.setRoles(roles);
            return mapToResponse(user);
        });
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setActive(user.isActive());
        response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return response;
    }
}
