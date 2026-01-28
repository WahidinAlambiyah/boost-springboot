package com.example.boost.controller;

import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.domain.dto.PermissionCreateRequest;
import com.example.boost.domain.dto.PermissionResponse;
import com.example.boost.domain.dto.PermissionUpdateRequest;
import com.example.boost.domain.mapper.PermissionMapper;
import com.example.boost.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions() {
        List<PermissionResponse> responses = permissionService.getPermissions().stream()
                .map(permissionMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Permissions retrieved", responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermission(@PathVariable UUID id) {
        PermissionResponse response = permissionMapper.toResponse(permissionService.getById(id));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Permission retrieved", response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_WRITE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        PermissionResponse response = permissionMapper.toResponse(permissionService.createPermission(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Permission created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_WRITE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(@PathVariable UUID id,
                                                                            @Valid @RequestBody PermissionUpdateRequest request) {
        PermissionResponse response = permissionMapper.toResponse(permissionService.updatePermission(id, request));
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Permission updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public ResponseEntity<ApiResponse<Object>> deletePermission(@PathVariable UUID id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Permission deleted", null));
    }
}
