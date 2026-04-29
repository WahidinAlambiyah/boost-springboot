package com.example.boost.service;

import com.example.boost.domain.dto.MenuNodeResponse;
import com.example.boost.domain.entity.Menu;
import com.example.boost.domain.entity.MenuMatchMode;
import com.example.boost.domain.entity.MenuPermission;
import com.example.boost.domain.entity.Permission;
import com.example.boost.iam.application.MenuService;
import com.example.boost.iam.infrastructure.MenuRepository;
import com.example.boost.iam.infrastructure.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private ObjectProvider<org.springframework.data.redis.core.StringRedisTemplate> redisTemplateProvider;

    @InjectMocks
    private MenuService menuService;

    @Test
    void adminGetsAllActiveMenus() {
        UUID userId = UUID.randomUUID();
        Menu dashboard = menu("DASHBOARD", "/dashboard", 1);
        Menu billing = menu("BILLING", "/billing", 2);
        Menu adminPanel = menu("ADMIN_PANEL", "/admin", 3);

        when(permissionRepository.findCodesByUserId(userId)).thenReturn(List.of("USER_READ", "BILLING_READ", "ADMIN_MANAGE"));
        when(menuRepository.findByIsActiveTrueAndIsVisibleTrueOrderByOrderNoAsc())
                .thenReturn(List.of(dashboard, billing, adminPanel));

        List<MenuNodeResponse> result = menuService.getMenuForCurrentUser(userId);

        assertThat(result).extracting(MenuNodeResponse::code)
                .containsExactly("DASHBOARD", "BILLING", "ADMIN_PANEL");
    }

    @Test
    void opsFinanceGuardianOnlyGetMenusMatchingPermission() {
        UUID userId = UUID.randomUUID();
        Menu dashboard = menu("DASHBOARD", "/dashboard", 1);
        Menu ops = protectedMenu("OPS", "/ops", 2, "OPS_EXECUTE");
        Menu finance = protectedMenu("FINANCE", "/finance", 3, "FINANCE_READ");
        Menu guardian = protectedMenu("GUARDIAN", "/guardian", 4, "GUARDIAN_VIEW");

        when(permissionRepository.findCodesByUserId(userId)).thenReturn(List.of("FINANCE_READ", "GUARDIAN_VIEW"));
        when(menuRepository.findByIsActiveTrueAndIsVisibleTrueOrderByOrderNoAsc())
                .thenReturn(List.of(dashboard, ops, finance, guardian));

        List<MenuNodeResponse> result = menuService.getMenuForCurrentUser(userId);

        assertThat(result).extracting(MenuNodeResponse::code)
                .containsExactly("DASHBOARD", "FINANCE", "GUARDIAN");
    }

    @Test
    void userWithoutPermissionDoesNotSeeRestrictedMenu() {
        UUID userId = UUID.randomUUID();
        Menu dashboard = menu("DASHBOARD", "/dashboard", 1);
        Menu restricted = protectedMenu("ADMIN_PANEL", "/admin", 2, "ADMIN_MANAGE");

        when(permissionRepository.findCodesByUserId(userId)).thenReturn(List.of());
        when(menuRepository.findByIsActiveTrueAndIsVisibleTrueOrderByOrderNoAsc())
                .thenReturn(List.of(dashboard, restricted));

        List<MenuNodeResponse> result = menuService.getMenuForCurrentUser(userId);

        assertThat(result).extracting(MenuNodeResponse::code)
                .containsExactly("DASHBOARD");
    }


    @Test
    void menuResponseContainsVisibilityFlags() {
        UUID userId = UUID.randomUUID();
        Menu dashboard = menu("DASHBOARD", "/dashboard", 1);

        when(permissionRepository.findCodesByUserId(userId)).thenReturn(List.of());
        when(menuRepository.findByIsActiveTrueAndIsVisibleTrueOrderByOrderNoAsc())
                .thenReturn(List.of(dashboard));

        List<MenuNodeResponse> result = menuService.getMenuForCurrentUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().visible()).isTrue();
        assertThat(result.getFirst().isActive()).isTrue();
    }

    private Menu menu(String code, String path, int orderNo) {
        return Menu.builder()
                .id(UUID.randomUUID())
                .code(code)
                .name(code)
                .path(path)
                .orderNo(orderNo)
                .isActive(true)
                .isVisible(true)
                .menuPermissions(Set.of())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private Menu protectedMenu(String code, String path, int orderNo, String permissionCode) {
        Menu menu = menu(code, path, orderNo);
        Permission permission = Permission.builder()
                .id(UUID.randomUUID())
                .code(permissionCode)
                .name(permissionCode)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        MenuPermission rule = MenuPermission.builder()
                .id(UUID.randomUUID())
                .menu(menu)
                .permission(permission)
                .matchMode(MenuMatchMode.ANY)
                .createdAt(OffsetDateTime.now())
                .build();

        menu.setMenuPermissions(Set.of(rule));
        return menu;
    }
}
