package com.example.boost.iam.api;

import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.*;
import com.example.boost.iam.application.MenuAdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "6. Menu Management")
@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
public class MenuAdminController {
    private final MenuAdminService menuAdminService;

    @GetMapping
    @PreAuthorize("hasAuthority('MENU_READ')")
    public ResponseEntity<ApiResponse<List<MenuAdminResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Menus retrieved", menuAdminService.getMenus()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MENU_WRITE')")
    public ResponseEntity<ApiResponse<MenuAdminResponse>> create(@Valid @RequestBody MenuAdminRequest request) {
        MenuAdminResponse response = menuAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Menu created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_WRITE')")
    public ResponseEntity<ApiResponse<MenuAdminResponse>> update(@PathVariable UUID id, @RequestBody MenuAdminUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Menu updated", menuAdminService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_WRITE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        menuAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('MENU_ASSIGN')")
    public ResponseEntity<Void> assign(@PathVariable UUID id, @Valid @RequestBody MenuPermissionAssignRequest request) {
        menuAdminService.assignPermission(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('MENU_ASSIGN')")
    public ResponseEntity<Void> unassign(@PathVariable UUID id, @PathVariable UUID permissionId) {
        menuAdminService.unassignPermission(id, permissionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reorder")
    @PreAuthorize("hasAuthority('MENU_WRITE')")
    public ResponseEntity<ApiResponse<MenuAdminResponse>> reorder(@PathVariable UUID id, @Valid @RequestBody MenuReorderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Menu reordered", menuAdminService.reorder(id, request)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('MENU_WRITE')")
    public ResponseEntity<ApiResponse<MenuAdminResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Menu activated", menuAdminService.setActive(id, true)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('MENU_WRITE')")
    public ResponseEntity<ApiResponse<MenuAdminResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Menu deactivated", menuAdminService.setActive(id, false)));
    }
}
