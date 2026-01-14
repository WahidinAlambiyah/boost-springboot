package com.example.graphqlusers.service;

import com.example.graphqlusers.app.AppException;
import com.example.graphqlusers.app.CreateUserInput;
import com.example.graphqlusers.app.ErrorCodes;
import com.example.graphqlusers.app.Role;
import com.example.graphqlusers.app.UpdateUserInput;
import com.example.graphqlusers.app.User;
import com.example.graphqlusers.app.UserPage;
import com.example.graphqlusers.auth.PasswordHasher;
import com.example.graphqlusers.repository.UserRepository;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class UserService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCodes.NOT_FOUND, "User not found"));
    }

    public UserPage listUsers(int page, int size) {
        List<User> users = userRepository.list(page, size);
        long total = userRepository.count();
        return new UserPage(users, page, size, total);
    }

    public User createUser(CreateUserInput input) {
        validateCreate(input.username(), input.email(), input.password(), input.roles());
        if (userRepository.findByUsername(input.username()).isPresent()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Username already exists");
        }
        if (userRepository.findByEmail(input.email()).isPresent()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Email already exists");
        }
        Instant now = Instant.now();
        Set<Role> roles = input.roles() == null || input.roles().isEmpty() ? Set.of(Role.USER) : input.roles();
        User user = new User(UUID.randomUUID(), input.username(), input.email(), input.fullName(),
                PasswordHasher.hash(input.password()), roles, now, now);
        try {
            return userRepository.create(user);
        } catch (RuntimeException e) {
            handleConstraint(e);
            throw e;
        }
    }

    public User updateUser(UUID id, UpdateUserInput input) {
        if (input.email() != null && !EMAIL_PATTERN.matcher(input.email()).matches()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Invalid email format");
        }
        if (input.password() != null && input.password().length() < 8) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Password must be at least 8 characters");
        }
        if (input.roles() != null && input.roles().isEmpty()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Roles cannot be empty");
        }
        String hash = input.password() == null ? null : PasswordHasher.hash(input.password());
        try {
            User updated = userRepository.update(id, input.email(), input.fullName(), hash, input.roles());
            if (updated == null) {
                throw new AppException(ErrorCodes.NOT_FOUND, "User not found");
            }
            return updated;
        } catch (RuntimeException e) {
            handleConstraint(e);
            throw e;
        }
    }

    public boolean deleteUser(UUID id) {
        return userRepository.delete(id);
    }

    private void validateCreate(String username, String email, String password, Set<Role> roles) {
        if (username == null || username.isBlank()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Username is required");
        }
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Valid email is required");
        }
        if (password == null || password.length() < 8) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Password must be at least 8 characters");
        }
        if (roles != null && roles.isEmpty()) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Roles cannot be empty");
        }
    }

    private void handleConstraint(RuntimeException e) {
        Throwable cause = e.getCause();
        if (cause instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
            throw new AppException(ErrorCodes.BAD_REQUEST, "Username or email already exists");
        }
    }
}
