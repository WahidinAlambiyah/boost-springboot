package com.example.boost.iam.api;

import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.domain.dto.UserCreateRequest;
import com.example.boost.domain.dto.UserDisableRequest;
import com.example.boost.domain.dto.UserIdentifierResponse;
import com.example.boost.domain.dto.UserPasswordUpdateRequest;
import com.example.boost.domain.dto.UserResponse;
import com.example.boost.domain.dto.UserRolesUpdateRequest;
import com.example.boost.domain.dto.UserUpdateRequest;
import com.example.boost.iam.application.UserService;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "3. User Management", description = "Manage users and their access")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(Pageable pageable,
                                                                    @RequestParam(value = "search", required = false) String search) {
        Page<UserResponse> page = userService.getUsers(pageable, search);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Users retrieved", page));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable UUID id) {
        UserResponse response = userService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User retrieved", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(Authentication authentication) {
        UserResponse response = userService.getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Profile retrieved", response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE') and hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserIdentifierResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserIdentifierResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "User created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_WRITE') and hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserIdentifierResponse>> updateUser(@PathVariable UUID id,
                                                                          @Valid @RequestBody UserUpdateRequest request) {
        UserIdentifierResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User updated", response));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_WRITE') and hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(@PathVariable UUID id,
                                                                 @Valid @RequestBody UserRolesUpdateRequest request) {
        UserResponse response = userService.assignRoles(id, request.getRoleCodes());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Roles updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE') and hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('USER_WRITE') and hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> updatePassword(@PathVariable UUID id,
                                               @Valid @RequestBody UserPasswordUpdateRequest request) {
        userService.changePassword(id, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('USER_DISABLE')")
    public ResponseEntity<Void> disableUser(@PathVariable UUID id,
                                            @Valid @RequestBody UserDisableRequest request) {
        userService.disableUser(id, request.getReason());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('USER_ENABLE')")
    public ResponseEntity<Void> enableUser(@PathVariable UUID id) {
        userService.enableUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('USER_UNLOCK')")
    public ResponseEntity<Void> unlockUser(@PathVariable UUID id) {
        userService.unlockUser(id);
        return ResponseEntity.noContent().build();
    }
}
