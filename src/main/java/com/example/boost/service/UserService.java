package com.example.boost.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.boost.domain.Role;
import com.example.boost.domain.User;
import com.example.boost.dto.RegisterUserRequest;
import com.example.boost.dto.UpdateUserRequest;
import com.example.boost.dto.UserResponse;
import com.example.boost.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    public UserService(UserRepository userRepository,
                       RoleService roleService,
                       PasswordEncoder passwordEncoder,
                       EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public User register(RegisterUserRequest request) {
        validatePassword(request.getPassword(), request.getConfirmPassword());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAgreementAt(request.getAgreementAt());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        Set<Role> roles = roleService.resolveRoles(request.getRoles());
        user.setRoles(roles);
        user.setActive(false);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        User savedUser = userRepository.save(user);
        emailVerificationService.sendVerification(savedUser);
        return savedUser;
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
            user.setPhoneNumber(request.getPhoneNumber());
            user.setAgreementAt(request.getAgreementAt());
            user.setActive(request.isActive());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                validatePasswordRules(request.getPassword());
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

    @Transactional
    public void recordFailedLogin(User user) {
        int attempts = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION));
        }
        userRepository.save(user);
    }

    @Transactional
    public void recordSuccessfulLogin(User user) {
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    public void ensureLoginAllowed(User user) {
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Akun Anda belum aktif, cek email verifikasi.");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "Akun terkunci sementara. Silakan coba lagi nanti.");
        }
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAgreementAt(user.getAgreementAt());
        response.setActive(user.isActive());
        response.setFailedLoginCount(user.getFailedLoginCount());
        response.setLockedUntil(user.getLockedUntil());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return response;
    }

    private void validatePassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Konfirmasi password tidak sesuai.");
        }
        validatePasswordRules(password);
    }

    private void validatePasswordRules(String password) {
        boolean meetsComplexity = password.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}");
        boolean isPassphrase = password.length() >= 15 && password.contains(" ");
        if (!meetsComplexity && !isPassphrase) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password harus kombinasi huruf besar, huruf kecil, angka, dan simbol atau passphrase minimal 15 karakter.");
        }
    }
}
