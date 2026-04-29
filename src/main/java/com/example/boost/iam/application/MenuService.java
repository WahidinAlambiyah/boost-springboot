package com.example.boost.iam.application;

import com.example.boost.domain.dto.MenuNodeResponse;
import com.example.boost.domain.entity.Menu;
import com.example.boost.domain.entity.MenuMatchMode;
import com.example.boost.domain.entity.MenuPermission;
import com.example.boost.iam.infrastructure.MenuRepository;
import com.example.boost.iam.infrastructure.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    private final Map<String, List<MenuNodeResponse>> inMemoryCache = new ConcurrentHashMap<>();

    @Value("${app.redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${app.menu.cache-ttl:PT30M}")
    private Duration cacheTtl;

    public List<MenuNodeResponse> getMenuForCurrentUser(UUID userId) {
        String key = key(userId);
        List<MenuNodeResponse> cached = readCache(key);
        if (cached != null) {
            return cached;
        }

        Set<String> userPermissions = new HashSet<>(permissionRepository.findCodesByUserId(userId));
        List<Menu> activeVisibleMenus = menuRepository.findByIsActiveTrueAndIsVisibleTrueOrderByOrderNoAsc();

        List<MenuNodeResponse> allowedMenus = activeVisibleMenus.stream()
                .filter(menu -> hasRequiredPermission(menu, userPermissions))
                .map(this::toFlatNode)
                .toList();

        List<MenuNodeResponse> tree = buildTree(allowedMenus);
        writeCache(key, tree);
        return tree;
    }

    public void evictUserMenuCache(UUID userId) {
        String key = key(userId);
        if (useRedis()) {
            redisTemplateProvider.getObject().delete(key);
            return;
        }
        inMemoryCache.remove(key);
    }

    public void evictAllMenuCaches() {
        if (useRedis()) {
            Set<String> keys = redisTemplateProvider.getObject().keys("menu:user:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplateProvider.getObject().delete(keys);
            }
            return;
        }
        inMemoryCache.clear();
    }

    private boolean hasRequiredPermission(Menu menu, Set<String> userPermissions) {
        Set<MenuPermission> rules = menu.getMenuPermissions();
        if (rules == null || rules.isEmpty()) {
            return true;
        }
        MenuMatchMode mode = rules.stream()
                .map(MenuPermission::getMatchMode)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(MenuMatchMode.ANY);

        Set<String> requiredPermissions = rules.stream()
                .map(menuPermission -> menuPermission.getPermission().getCode())
                .collect(Collectors.toSet());

        if (mode == MenuMatchMode.ALL) {
            return userPermissions.containsAll(requiredPermissions);
        }
        return requiredPermissions.stream().anyMatch(userPermissions::contains);
    }

    private MenuNodeResponse toFlatNode(Menu menu) {
        return MenuNodeResponse.builder()
                .id(menu.getId())
                .parentId(menu.getParent() != null ? menu.getParent().getId() : null)
                .code(menu.getCode())
                .name(menu.getName())
                .path(menu.getPath())
                .icon(menu.getIcon())
                .orderNo(menu.getOrderNo())
                .children(new ArrayList<>())
                .build();
    }

    private List<MenuNodeResponse> buildTree(List<MenuNodeResponse> flat) {
        Map<UUID, MenuNodeResponse> index = flat.stream().collect(Collectors.toMap(MenuNodeResponse::id, node -> node));
        List<MenuNodeResponse> roots = new ArrayList<>();

        for (MenuNodeResponse node : flat) {
            if (node.parentId() == null || !index.containsKey(node.parentId())) {
                roots.add(node);
                continue;
            }
            index.get(node.parentId()).children().add(node);
        }

        sortTree(roots);
        return roots;
    }

    private void sortTree(List<MenuNodeResponse> nodes) {
        nodes.sort(Comparator.comparing(MenuNodeResponse::orderNo));
        for (MenuNodeResponse node : nodes) {
            sortTree(node.children());
        }
    }

    private String key(UUID userId) {
        return "menu:user:" + userId;
    }

    private List<MenuNodeResponse> readCache(String key) {
        if (!useRedis()) {
            return inMemoryCache.get(key);
        }
        return null;
    }

    private void writeCache(String key, List<MenuNodeResponse> tree) {
        if (!useRedis()) {
            inMemoryCache.put(key, tree);
            return;
        }
        // TODO: serialize JSON payload if Redis cache storage for menu tree is required.
        inMemoryCache.put(key, tree);
    }

    private boolean useRedis() {
        return redisEnabled && redisTemplateProvider.getIfAvailable() != null;
    }
}
