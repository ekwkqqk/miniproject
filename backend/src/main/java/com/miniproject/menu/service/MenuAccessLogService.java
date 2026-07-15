package com.miniproject.menu.service;

import com.miniproject.menu.domain.Menu;
import com.miniproject.menu.domain.MenuAccessLog;
import com.miniproject.menu.domain.MenuAccessLogRepository;
import com.miniproject.menu.domain.MenuRepository;
import com.miniproject.menu.dto.MenuAccessLogResponse;
import com.miniproject.user.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MenuAccessLogService {

    public static final String ACCESS_TYPE_MENU = "MENU";
    public static final String ACCESS_TYPE_API = "API";
    public static final String ATTR_MENU_URL = "MENU_ACCESS_URL";
    public static final String ATTR_ACCESS_TYPE = "MENU_ACCESS_TYPE";

    private static final Logger log = LoggerFactory.getLogger(MenuAccessLogService.class);

    private final MenuAccessLogRepository menuAccessLogRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    /** leaf menus sorted by URL length desc for prefix match */
    private final AtomicReference<List<MenuUrlRef>> leafUrlCache = new AtomicReference<>();

    public MenuAccessLogService(MenuAccessLogRepository menuAccessLogRepository,
                                MenuRepository menuRepository,
                                UserRepository userRepository) {
        this.menuAccessLogRepository = menuAccessLogRepository;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
    }

    /**
     * Enrichment + insert on async thread so the HTTP request path pays no DB cost.
     */
    @Async
    public void logAccessAsync(String email,
                               String menuUrl,
                               String httpMethod,
                               String requestUri,
                               String clientIp,
                               String userAgent,
                               Integer httpStatus,
                               String accessType) {
        try {
            MenuAccessLog accessLog = buildLog(
                    email, menuUrl, httpMethod, requestUri, clientIp, userAgent, httpStatus, accessType
            );
            menuAccessLogRepository.insert(accessLog);
        } catch (Exception ex) {
            log.warn("Failed to save menu access log: {}", ex.getMessage());
        }
    }

    public void invalidateMenuUrlCache() {
        leafUrlCache.set(null);
    }

    MenuAccessLog buildLog(String email,
                           String menuUrl,
                           String httpMethod,
                           String requestUri,
                           String clientIp,
                           String userAgent,
                           Integer httpStatus,
                           String accessType) {
        MenuAccessLog accessLog = new MenuAccessLog();
        accessLog.setUserEmail(email);
        accessLog.setHttpMethod(httpMethod);
        accessLog.setRequestUri(requestUri);
        accessLog.setClientIp(truncate(clientIp, 64));
        accessLog.setUserAgent(truncate(userAgent, 500));
        accessLog.setHttpStatus(httpStatus);
        accessLog.setAccessType(accessType);
        accessLog.setAccessedAt(LocalDateTime.now());

        userRepository.findByEmail(email).ifPresent(user -> accessLog.setUserId(user.getId()));

        String normalized = normalizeMenuUrl(menuUrl);
        if (normalized != null) {
            accessLog.setMenuUrl(normalized);
            resolveMenuRef(normalized).ifPresent(ref -> {
                accessLog.setMenuId(ref.id());
                accessLog.setMenuUrl(ref.url());
                accessLog.setMenuName(ref.name());
            });
        }
        return accessLog;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRecentLogs(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 200);
        int offset = (safePage - 1) * safeSize;
        List<MenuAccessLogResponse> items = MenuAccessLogResponse.fromList(
                menuAccessLogRepository.findRecent(safeSize, offset)
        );
        long total = menuAccessLogRepository.countAll();
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("page", safePage);
        result.put("size", safeSize);
        result.put("total", total);
        return result;
    }

    public static String normalizeMenuUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String value = url.trim();
        if (value.startsWith("/api")) {
            value = value.substring(4);
        }
        if (value.isBlank()) {
            value = "/";
        }
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        int q = value.indexOf('?');
        if (q >= 0) {
            value = value.substring(0, q);
        }
        if (value.length() > 1 && value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private Optional<MenuUrlRef> resolveMenuRef(String path) {
        List<MenuUrlRef> leaves = leafMenus();
        for (MenuUrlRef leaf : leaves) {
            if (path.equals(leaf.url()) || path.startsWith(leaf.url() + "/")) {
                return Optional.of(leaf);
            }
        }
        return Optional.empty();
    }

    private List<MenuUrlRef> leafMenus() {
        List<MenuUrlRef> cached = leafUrlCache.get();
        if (cached != null) {
            return cached;
        }
        synchronized (leafUrlCache) {
            cached = leafUrlCache.get();
            if (cached != null) {
                return cached;
            }
            List<MenuUrlRef> loaded = menuRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                    .filter(menu -> !menu.isFolder() && menu.getUrl() != null && !menu.getUrl().isBlank())
                    .map(menu -> new MenuUrlRef(menu.getId(), menu.getUrl(), menu.getName()))
                    .sorted(Comparator.comparingInt((MenuUrlRef m) -> m.url().length()).reversed())
                    .toList();
            leafUrlCache.set(loaded);
            return loaded;
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private record MenuUrlRef(Long id, String url, String name) {
    }
}
