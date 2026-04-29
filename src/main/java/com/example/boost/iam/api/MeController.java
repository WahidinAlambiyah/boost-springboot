package com.example.boost.iam.api;

import com.example.boost.common.api.ApiResponse;
import com.example.boost.domain.dto.MenuNodeResponse;
import com.example.boost.iam.application.MenuService;
import com.example.boost.security.CurrentActorProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Tag(name = "9. Me")
public class MeController {
    private final MenuService menuService;
    private final CurrentActorProvider currentActorProvider;

    @GetMapping("/menu")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user menu")
    public ResponseEntity<ApiResponse<List<MenuNodeResponse>>> getMyMenu() {
        UUID userId = currentActorProvider.currentUserId();
        List<MenuNodeResponse> response = menuService.getMenuForCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Menu retrieved", response));
    }
}
