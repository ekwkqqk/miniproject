package com.miniproject.config;

import com.miniproject.role.service.RoleService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SystemAdminApiFilter extends OncePerRequestFilter {

    private static final String SYSTEM_ADMIN_AUTHORITY = "ROLE_" + RoleService.SYSTEM_ADMIN_CODE;

    private final SecurityErrorHandler securityErrorHandler;

    public SystemAdminApiFilter(SecurityErrorHandler securityErrorHandler) {
        this.securityErrorHandler = securityErrorHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof String)
                    || !hasSystemAdmin(authentication)) {
                securityErrorHandler.handle(request, response, new AccessDeniedException("시스템 관리자만 접근할 수 있습니다."));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean hasSystemAdmin(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (SYSTEM_ADMIN_AUTHORITY.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
