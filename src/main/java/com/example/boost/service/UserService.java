package com.example.boost.service;

import com.example.boost.domain.dto.UserCreateRequest;
import com.example.boost.domain.dto.UserUpdateRequest;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.User;
import com.example.boost.exception.ConflictException;
import com.example.boost.exception.NotFoundException;
import com.example.boost.repository.RoleRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public User createUser(UserCreateRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setRoles(resolveRoles(request.getRoleCodes()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, UserUpdateRequest request) {
        User user = getById(id);
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new ConflictException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getIsActive() != null) {
            user.setActive(request.getIsActive());
        }
        return userRepository.save(user);
    }

    @Transactional
    public User assignRoles(UUID id, Set<String> roleCodes) {
        User user = getById(id);
        user.setRoles(resolveRoles(roleCodes));
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

    private Set<Role> resolveRoles(Set<String> roleCodes) {
        Set<String> codes = roleCodes == null || roleCodes.isEmpty() ? Set.of("USER") : roleCodes;
        List<Role> roles = roleRepository.findByCodeIn(codes);
        if (roles.size() != codes.size()) {
            Set<String> found = new HashSet<>();
            for (Role role : roles) {
                found.add(role.getCode());
            }
            Set<String> missing = new HashSet<>(codes);
            missing.removeAll(found);
            throw new NotFoundException("Role codes not found: " + missing);
        }
        return new HashSet<>(roles);
    }
}
