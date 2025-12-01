package com.example.boost.service;

import com.example.boost.domain.Role;
import com.example.boost.domain.User;
import com.example.boost.dto.RegisterUserRequest;
import com.example.boost.dto.UserResponse;
import com.example.boost.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<Role> roles = roleService.resolveRoles(request.getRoles());
        user.setRoles(roles);
        user.setActive(true);
        return userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<UserResponse> assignRoles(Long userId, Set<String> roleNames) {
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
