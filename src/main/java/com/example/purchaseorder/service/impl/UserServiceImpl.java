package com.example.purchaseorder.service.impl;

import com.example.purchaseorder.domain.User;
import com.example.purchaseorder.dto.UserRequest;
import com.example.purchaseorder.exception.ResourceNotFoundException;
import com.example.purchaseorder.repository.UserRepository;
import com.example.purchaseorder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(get(id));
    }

    @Override
    @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    private void applyRequest(User user, UserRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setCreatedBy(request.getCreatedBy());
        user.setCreatedDatetime(request.getCreatedDatetime());
        user.setUpdatedBy(request.getUpdatedBy());
        user.setUpdatedDatetime(request.getUpdatedDatetime());
    }

    private User createUser(UserRequest request) {
        User user = new User();
        applyRequest(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }
}
