package com.alambiyah.userauth.application.service;

import com.alambiyah.userauth.domain.entity.User;
import com.alambiyah.userauth.domain.model.Role;
import com.alambiyah.userauth.dto.UserCreateRequest;
import com.alambiyah.userauth.dto.UserUpdateRequest;
import com.alambiyah.userauth.framework.cache.UserCache;
import com.alambiyah.userauth.framework.exception.ConflictException;
import com.alambiyah.userauth.framework.exception.ResourceNotFoundException;
import com.alambiyah.userauth.framework.persistence.UserRepository;
import com.alambiyah.userauth.framework.security.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final UserCache userCache;

    @Inject
    public UserService(UserRepository userRepository, PasswordService passwordService, UserCache userCache) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.userCache = userCache;
    }

    @Transactional
    public User register(String username, String email, String fullName, String password) {
        ensureUnique(username, email);
        User user = new User();
        user.username = username;
        user.email = email;
        user.fullName = fullName;
        user.passwordHash = passwordService.hash(password);
        user.roles = Set.of(Role.USER);
        user.isActive = true;
        userRepository.persist(user);
        return user;
    }

    @Transactional
    public User create(UserCreateRequest request) {
        ensureUnique(request.username(), request.email());
        User user = new User();
        user.username = request.username();
        user.email = request.email();
        user.fullName = request.fullName();
        user.passwordHash = passwordService.hash(request.password());
        user.roles = parseRoles(request.roles(), Set.of(Role.USER));
        user.isActive = request.isActive() == null || request.isActive();
        userRepository.persist(user);
        return user;
    }

    public List<User> listAll() {
        return userRepository.listAll();
    }

    public User getById(UUID id) {
        return userRepository.findOptionalById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User update(UUID id, UserUpdateRequest request) {
        User user = getById(id);
        if (request.username() != null && !request.username().equals(user.username)) {
            if (userRepository.existsByUsername(request.username())) {
                throw new ConflictException("Username already exists");
            }
            user.username = request.username();
        }
        if (request.email() != null && !request.email().equals(user.email)) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ConflictException("Email already exists");
            }
            user.email = request.email();
        }
        if (request.fullName() != null) {
            user.fullName = request.fullName();
        }
        if (request.password() != null) {
            user.passwordHash = passwordService.hash(request.password());
        }
        if (request.roles() != null) {
            user.roles = parseRoles(request.roles(), user.roles);
        }
        if (request.isActive() != null) {
            user.isActive = request.isActive();
        }
        userCache.evict(user.id);
        return user;
    }

    @Transactional
    public void delete(UUID id) {
        User user = getById(id);
        userRepository.delete(user);
        userCache.evict(id);
    }

    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    private void ensureUnique(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
    }

    private Set<Role> parseRoles(Set<String> roles, Set<Role> fallback) {
        if (roles == null || roles.isEmpty()) {
            return fallback;
        }
        return roles.stream()
                .map(String::toUpperCase)
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }
}
