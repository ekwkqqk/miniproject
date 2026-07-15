package com.miniproject.config;

import com.miniproject.menu.service.MenuAccessLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 인증된 사용자의 메뉴/API 접근을 {@code pjt_menu_access_logs}에 기록한다.
 * <ul>
 *   <li>MENU: 프론트 라우트 변경 시 {@code POST /api/menus/access} (request attribute)</li>
 *   <li>API: 메뉴 URL과 매칭되는 인증 API 호출</li>
 * </ul>
 */
@Component
public class MenuAccessLogInterceptor implements HandlerInterceptor {

    private final MenuAccessLogService menuAccessLogService;

    public MenuAccessLogInterceptor(MenuAccessLogService menuAccessLogService) {
        this.menuAccessLogService = menuAccessLogService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof String email)
                || email.isBlank()) {
            return;
        }

        String uri = request.getRequestURI();
        if (shouldSkip(uri, request.getMethod())) {
            return;
        }

        String accessType = (String) request.getAttribute(MenuAccessLogService.ATTR_ACCESS_TYPE);
        String menuUrl = (String) request.getAttribute(MenuAccessLogService.ATTR_MENU_URL);

        if (menuUrl == null) {
            menuUrl = MenuAccessLogService.normalizeMenuUrl(uri);
            accessType = MenuAccessLogService.ACCESS_TYPE_API;
            // API 자동 기록은 /api/admin, /api/menus 등 업무 API만
            if (!isBusinessApi(uri)) {
                return;
            }
        } else if (accessType == null) {
            accessType = MenuAccessLogService.ACCESS_TYPE_MENU;
        }

        menuAccessLogService.logAccessAsync(
                email,
                menuUrl,
                request.getMethod(),
                uri,
                resolveClientIp(request),
                request.getHeader("User-Agent"),
                response.getStatus(),
                accessType
        );
    }

    private boolean shouldSkip(String uri, String method) {
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        return uri.startsWith("/api/auth/")
                || uri.equals("/api/health")
                || uri.equals("/api/settings/public")
                || uri.equals("/api/menus/my")
                || uri.startsWith("/api/admin/menu-access-logs")
                || uri.startsWith("/swagger")
                || uri.startsWith("/v3/api-docs");
    }

    private boolean isBusinessApi(String uri) {
        return uri.startsWith("/api/admin/");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
