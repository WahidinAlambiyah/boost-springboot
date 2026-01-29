package com.example.boost.controller;

import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.domain.dto.UserCreateRequest;
import com.example.boost.domain.dto.UserDisableRequest;
import com.example.boost.domain.dto.UserPasswordUpdateRequest;
import com.example.boost.domain.dto.UserResponse;
import com.example.boost.domain.dto.UserRolesUpdateRequest;
import com.example.boost.domain.dto.UserUpdateRequest;
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
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(Pageable pageable) {
        Page<UserResponse> page = userService.getUsers(pageable).map(userMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Users retrieved", page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID id) {
        UserResponse response = userMapper.toResponse(userService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User retrieved", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(Authentication authentication) {
        UserResponse response = userMapper.toResponse(userService.getCurrentUser(authentication));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Profile retrieved", response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userMapper.toResponse(userService.createUser(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "User created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable UUID id,
                                                                @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userMapper.toResponse(userService.updateUser(id, request));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User updated", response));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(@PathVariable UUID id,
                                                                 @Valid @RequestBody UserRolesUpdateRequest request) {
        UserResponse response = userMapper.toResponse(userService.assignRoles(id, request.getRoleCodes()));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Roles updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User deleted", null));
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<ApiResponse<Object>> updatePassword(@PathVariable UUID id,
                                                              @Valid @RequestBody UserPasswordUpdateRequest request) {
        userService.changePassword(id, request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Password updated", null));
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('USER_DISABLE')")
    public ResponseEntity<ApiResponse<Object>> disableUser(@PathVariable UUID id,
                                                           @Valid @RequestBody UserDisableRequest request) {
        userService.disableUser(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User disabled", null));
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('USER_ENABLE')")
    public ResponseEntity<ApiResponse<Object>> enableUser(@PathVariable UUID id) {
        userService.enableUser(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User enabled", null));
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('USER_UNLOCK')")
    public ResponseEntity<ApiResponse<Object>> unlockUser(@PathVariable UUID id) {
        userService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User unlocked", null));
    }
}
