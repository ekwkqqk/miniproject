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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public MenuAccessLogService(MenuAccessLogRepository menuAccessLogRepository,
                                MenuRepository menuRepository,
                                UserRepository userRepository) {
        this.menuAccessLogRepository = menuAccessLogRepository;
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
    }

    @Async
    public void saveAsync(MenuAccessLog accessLog) {
        try {
            menuAccessLogRepository.insert(accessLog);
        } catch (Exception ex) {
            log.warn("Failed to save menu access log: {}", ex.getMessage());
        }
    }

    public MenuAccessLog buildLog(String email,
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
            menuRepository.findByUrl(normalized).ifPresentOrElse(menu -> {
                accessLog.setMenuId(menu.getId());
                accessLog.setMenuName(menu.getName());
            }, () -> {
                // longest prefix match for parameterized routes e.g. /demo/view/1001
                findBestMenuMatch(normalized).ifPresent(menu -> {
                    accessLog.setMenuId(menu.getId());
                    accessLog.setMenuUrl(menu.getUrl());
                    accessLog.setMenuName(menu.getName());
                });
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
        // drop query string
        int q = value.indexOf('?');
        if (q >= 0) {
            value = value.substring(0, q);
        }
        if (value.length() > 1 && value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private Optional<Menu> findBestMenuMatch(String path) {
        return menuRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .filter(menu -> !menu.isFolder() && menu.getUrl() != null)
                .filter(menu -> path.equals(menu.getUrl()) || path.startsWith(menu.getUrl() + "/"))
                .max((a, b) -> Integer.compare(a.getUrl().length(), b.getUrl().length()));
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
