package com.example.boost.controller;

import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.domain.dto.CreateUserRequest;
import com.example.boost.domain.dto.UpdateUserRequest;
import com.example.boost.domain.dto.UserResponse;
import com.example.boost.domain.mapper.UserMapper;
import com.example.boost.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(Pageable pageable) {
        Page<UserResponse> page = userService.getUsers(pageable).map(userMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Users retrieved", page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID id) {
        UserResponse response = userMapper.toResponse(userService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User retrieved", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(Authentication authentication) {
        UserResponse response = userMapper.toResponse(userService.getCurrentUser(authentication));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Profile retrieved", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userMapper.toResponse(userService.createUser(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "User created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable UUID id,
                                                                @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userMapper.toResponse(userService.updateUser(id, request));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User deleted", null));
    }
}
