package com.example.purchaseorder.service.impl;

import com.example.purchaseorder.domain.User;
import com.example.purchaseorder.dto.UserPatchRequest;
import com.example.purchaseorder.dto.UserRequest;
import com.example.purchaseorder.exception.ResourceNotFoundException;
import com.example.purchaseorder.repository.UserRepository;
import com.example.purchaseorder.service.UserService;
import com.example.purchaseorder.service.util.AuditUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User create(UserRequest request) {
        return createUser(request);
    }

    @Override
    public List<User> createBulk(List<UserRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }
        return requests.stream()
                .map(this::createUser)
                .collect(Collectors.toList());
    }

    @Override
    public User update(Long id, UserRequest request) {
        User user = get(id);
        applyRequest(user, request);
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        applyUpdateAudit(user);
        return userRepository.save(user);
    }

    @Override
    public User patch(Long id, UserPatchRequest request) {
        User user = get(id);
        applyPatch(user, request);
        applyUpdateAudit(user);
        return userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    public void deletePermanent(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> list(Pageable pageable) {
        return userRepository.findAllByDeletedFalse(pageable);
    }

    private void applyRequest(User user, UserRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
    }

    private void applyPatch(User user, UserPatchRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
    }

    private User createUser(UserRequest request) {
        User user = new User();
        applyRequest(user, request);
        applyCreationAudit(user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDeleted(false);
        return userRepository.save(user);
    }

    private void applyCreationAudit(User user) {
        String auditor = AuditUtils.resolveCurrentAuditor();
        OffsetDateTime now = AuditUtils.currentDateTime();
        user.setCreatedBy(auditor);
        user.setCreatedDatetime(now);
        user.setUpdatedBy(null);
        user.setUpdatedDatetime(null);
    }

    private void applyUpdateAudit(User user) {
        String auditor = AuditUtils.resolveCurrentAuditor();
        OffsetDateTime now = AuditUtils.currentDateTime();
        user.setUpdatedBy(auditor);
        user.setUpdatedDatetime(now);
    }
}
