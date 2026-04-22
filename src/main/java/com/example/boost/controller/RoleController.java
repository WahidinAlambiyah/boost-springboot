package com.example.boost.controller;

import com.example.boost.domain.dto.ApiResponse;
import com.example.boost.domain.dto.IdNameResponse;
import com.example.boost.domain.dto.RoleCreateRequest;
import com.example.boost.domain.dto.RolePermissionsUpdateRequest;
import com.example.boost.domain.dto.RoleResponse;
import com.example.boost.domain.dto.RoleUpdateRequest;
import com.example.boost.service.RoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "4. Role Management")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<Page<RoleResponse>>> getRoles(Pageable pageable) {
        Page<RoleResponse> responses = roleService.getRoles(pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Roles retrieved", responses));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<List<IdNameResponse>>> getRoleLookups() {
        List<IdNameResponse> responses = roleService.getRoleLookups();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Roles retrieved", responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable UUID id) {
        RoleResponse response = roleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Role retrieved", response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleCreateRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Role created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(@PathVariable UUID id,
                                                                @Valid @RequestBody RoleUpdateRequest request) {
        RoleResponse response = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Role updated", response));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissions(@PathVariable UUID id,
                                                                       @Valid @RequestBody RolePermissionsUpdateRequest request) {
        RoleResponse response = roleService.assignPermissions(id, request.getPermissionCodes());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Permissions updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
