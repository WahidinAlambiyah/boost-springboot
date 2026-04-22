package com.example.boost.service;

import com.example.boost.domain.dto.UserCreateRequest;
import com.example.boost.domain.dto.UserUpdateRequest;
import com.example.boost.domain.entity.Role;
import com.example.boost.domain.entity.User;
import com.example.boost.exception.ConflictException;
import com.example.boost.exception.NotFoundException;
import com.example.boost.domain.dto.UserIdentifierResponse;
import com.example.boost.domain.dto.UserResponse;
import com.example.boost.domain.mapper.UserMapper;
import com.example.boost.repository.RoleRepository;
import com.example.boost.repository.UserRepository;
import com.example.boost.repository.UserSpecifications;
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
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable, String search) {
        return userRepository.findAll(UserSpecifications.searchByKeyword(search), pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        User user = userRepository.findWithRolesById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserIdentifierResponse createUser(UserCreateRequest request) {
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
        User saved = userRepository.save(user);
        return userMapper.toIdentifier(saved);
    }

    @Transactional
    public UserIdentifierResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findWithRolesById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        boolean wasActive = user.isActive();
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
        User saved = userRepository.save(user);
        if (request.getIsActive() != null && wasActive != saved.isActive()) {
            auditLogService.dataEvent(saved.isActive() ? "USER_ENABLED" : "USER_DISABLED")
                    .entity("USER", saved.getId().toString())
                    .before(java.util.Map.of("isActive", wasActive))
                    .after(java.util.Map.of("isActive", saved.isActive()))
                    .statusSuccess()
                    .save();
        }
        return userMapper.toIdentifier(saved);
    }

    @Transactional
    public UserResponse assignRoles(UUID id, Set<String> roleCodes) {
        User user = userRepository.findWithRolesById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Set<String> beforeRoles = user.getRoleCodes();
        user.setRoles(resolveRoles(roleCodes));
        User saved = userRepository.save(user);
        Set<String> afterRoles = saved.getRoleCodes();
        Set<String> added = new HashSet<>(afterRoles);
        added.removeAll(beforeRoles);
        Set<String> removed = new HashSet<>(beforeRoles);
        removed.removeAll(afterRoles);
        if (!added.isEmpty()) {
            auditLogService.rbacEvent("USER_ROLE_ASSIGNED")
                    .entity("USER", saved.getId().toString())
                    .metadata(java.util.Map.of("roles", added))
                    .statusSuccess()
                    .save();
        }
        if (!removed.isEmpty()) {
            auditLogService.rbacEvent("USER_ROLE_REMOVED")
                    .entity("USER", saved.getId().toString())
                    .metadata(java.util.Map.of("roles", removed))
                    .statusSuccess()
                    .save();
        }
        return userMapper.toResponse(saved);
    }

    @Transactional
    public void softDeleteUser(UUID id) {
        User user = userRepository.findWithRolesById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setDeletedAt(OffsetDateTime.now());
        userRepository.save(user);
        auditLogService.dataEvent("USER_DELETED")
                .entity("USER", user.getId().toString())
                .statusSuccess()
                .save();
    }

    @Transactional
    public void changePassword(UUID id, String newPassword) {
        User user = userRepository.findWithRolesById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(OffsetDateTime.now());
        userRepository.save(user);
        auditLogService.securityEvent("PASSWORD_CHANGED")
                .entity("USER", user.getId().toString())
                .statusSuccess()
                .save();
    }

    @Transactional
    public void disableUser(UUID id, String reason) {
        User user = userRepository.findWithRolesById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setActive(false);
        user.setDisabledAt(OffsetDateTime.now());
        user.setDisabledReason(reason);
        userRepository.save(user);
        auditLogService.securityEvent("USER_DISABLED")
                .entity("USER", user.getId().toString())
                .statusSuccess()
                .metadata(java.util.Map.of("reason", reason))
                .save();
    }

    @Transactional
    public void enableUser(UUID id) {
        User user = userRepository.findWithRolesById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setActive(true);
        user.setDisabledAt(null);
        user.setDisabledReason(null);
        userRepository.save(user);
        auditLogService.securityEvent("USER_ENABLED")
                .entity("USER", user.getId().toString())
                .statusSuccess()
                .save();
    }

    @Transactional
    public void unlockUser(UUID id) {
        User user = userRepository.findWithRolesById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        auditLogService.securityEvent("ACCOUNT_UNLOCKED")
                .entity("USER", user.getId().toString())
                .statusSuccess()
                .save();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new NotFoundException("User not found");
        }
        User user = userRepository.findWithRolesById(principal.getUser().getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toResponse(user);
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
