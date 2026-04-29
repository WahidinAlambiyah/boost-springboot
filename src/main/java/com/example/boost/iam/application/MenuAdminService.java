package com.example.boost.iam.application;

import com.example.boost.common.exception.BadRequestException;
import com.example.boost.common.exception.ConflictException;
import com.example.boost.common.exception.NotFoundException;
import com.example.boost.domain.dto.*;
import com.example.boost.domain.entity.Menu;
import com.example.boost.domain.entity.MenuMatchMode;
import com.example.boost.domain.entity.MenuPermission;
import com.example.boost.domain.entity.Permission;
import com.example.boost.iam.infrastructure.MenuPermissionRepository;
import com.example.boost.iam.infrastructure.MenuRepository;
import com.example.boost.iam.infrastructure.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuAdminService {
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final MenuPermissionRepository menuPermissionRepository;
    private final MenuService menuService;

    @Transactional(readOnly = true)
    public List<MenuAdminResponse> getMenus() {
        return menuRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('MENU_WRITE')")
    public MenuAdminResponse create(MenuAdminRequest request) {
        if (menuRepository.findAll().stream().anyMatch(m -> m.getCode().equalsIgnoreCase(request.getCode()))) {
            throw new ConflictException("Menu code already exists");
        }
        Menu menu = new Menu();
        applyCreateRequest(menu, request);
        validateNoCycle(menu, request.getParentId());
        Menu saved = menuRepository.save(menu);
        menuService.evictAllMenuCaches();
        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAuthority('MENU_WRITE')")
    public MenuAdminResponse update(UUID id, MenuAdminUpdateRequest request) {
        Menu menu = findMenu(id);
        if (request.getParentId() != null || request.getParentId() == null) {
            validateNoCycle(menu, request.getParentId());
            menu.setParent(request.getParentId() == null ? null : findMenu(request.getParentId()));
        }
        if (request.getName() != null) menu.setName(request.getName());
        if (request.getPath() != null) menu.setPath(request.getPath());
        if (request.getIcon() != null) menu.setIcon(request.getIcon());
        if (request.getOrderNo() != null) menu.setOrderNo(request.getOrderNo());
        if (request.getIsVisible() != null) menu.setVisible(request.getIsVisible());
        if (request.getIsActive() != null) menu.setActive(request.getIsActive());
        Menu saved = menuRepository.save(menu);
        menuService.evictAllMenuCaches();
        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAuthority('MENU_WRITE')")
    public void delete(UUID id) {
        menuRepository.delete(findMenu(id));
        menuService.evictAllMenuCaches();
    }

    @Transactional
    @PreAuthorize("hasAuthority('MENU_ASSIGN')")
    public void assignPermission(UUID menuId, MenuPermissionAssignRequest request) {
        Menu menu = findMenu(menuId);
        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new NotFoundException("Permission not found"));
        if (menuPermissionRepository.existsByMenuIdAndPermissionId(menuId, permission.getId())) return;
        MenuPermission mp = new MenuPermission();
        mp.setMenu(menu);
        mp.setPermission(permission);
        mp.setMatchMode(request.getMatchMode() == null ? MenuMatchMode.ANY : request.getMatchMode());
        menuPermissionRepository.save(mp);
        menuService.evictAllMenuCaches();
    }

    @Transactional
    @PreAuthorize("hasAuthority('MENU_ASSIGN')")
    public void unassignPermission(UUID menuId, UUID permissionId) {
        menuPermissionRepository.deleteByMenuIdAndPermissionId(menuId, permissionId);
        menuService.evictAllMenuCaches();
    }

    @Transactional
    @PreAuthorize("hasAuthority('MENU_WRITE')")
    public MenuAdminResponse reorder(UUID id, MenuReorderRequest request) {
        Menu menu = findMenu(id);
        menu.setOrderNo(request.getOrderNo());
        menuService.evictAllMenuCaches();
        return toResponse(menuRepository.save(menu));
    }

    @Transactional
    @PreAuthorize("hasAuthority('MENU_WRITE')")
    public MenuAdminResponse setActive(UUID id, boolean active) {
        Menu menu = findMenu(id);
        menu.setActive(active);
        menuService.evictAllMenuCaches();
        return toResponse(menuRepository.save(menu));
    }

    private void applyCreateRequest(Menu menu, MenuAdminRequest request) {
        menu.setCode(request.getCode());
        menu.setName(request.getName());
        menu.setPath(request.getPath());
        menu.setIcon(request.getIcon());
        menu.setOrderNo(request.getOrderNo());
        menu.setActive(request.getIsActive() == null || request.getIsActive());
        menu.setVisible(request.getIsVisible() == null || request.getIsVisible());
        menu.setParent(request.getParentId() == null ? null : findMenu(request.getParentId()));
    }

    private void validateNoCycle(Menu current, UUID newParentId) {
        if (current.getId() == null || newParentId == null) return;
        if (current.getId().equals(newParentId)) throw new BadRequestException("Parent menu cannot be itself");
        Menu parent = findMenu(newParentId);
        while (parent != null) {
            if (parent.getId().equals(current.getId())) {
                throw new BadRequestException("Cyclic parent-child relationship is not allowed");
            }
            parent = parent.getParent();
        }
    }

    private Menu findMenu(UUID id) {
        return menuRepository.findById(id).orElseThrow(() -> new NotFoundException("Menu not found"));
    }

    private MenuAdminResponse toResponse(Menu menu) {
        return MenuAdminResponse.builder()
                .id(menu.getId())
                .parentId(menu.getParent() == null ? null : menu.getParent().getId())
                .code(menu.getCode())
                .name(menu.getName())
                .path(menu.getPath())
                .icon(menu.getIcon())
                .orderNo(menu.getOrderNo())
                .isActive(menu.isActive())
                .isVisible(menu.isVisible())
                .build();
    }
}
