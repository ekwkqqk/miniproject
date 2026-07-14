package com.miniproject.menu.dto;

import com.miniproject.menu.domain.MenuAccessLog;

import java.time.LocalDateTime;
import java.util.List;

public class MenuAccessLogResponse {

    private final Long id;
    private final Long userId;
    private final String userEmail;
    private final Long menuId;
    private final String menuUrl;
    private final String menuName;
    private final String httpMethod;
    private final String requestUri;
    private final String clientIp;
    private final String userAgent;
    private final Integer httpStatus;
    private final String accessType;
    private final LocalDateTime accessedAt;

    public MenuAccessLogResponse(MenuAccessLog log) {
        this.id = log.getId();
        this.userId = log.getUserId();
        this.userEmail = log.getUserEmail();
        this.menuId = log.getMenuId();
        this.menuUrl = log.getMenuUrl();
        this.menuName = log.getMenuName();
        this.httpMethod = log.getHttpMethod();
        this.requestUri = log.getRequestUri();
        this.clientIp = log.getClientIp();
        this.userAgent = log.getUserAgent();
        this.httpStatus = log.getHttpStatus();
        this.accessType = log.getAccessType();
        this.accessedAt = log.getAccessedAt();
    }

    public static List<MenuAccessLogResponse> fromList(List<MenuAccessLog> logs) {
        return logs.stream().map(MenuAccessLogResponse::new).toList();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Long getMenuId() {
        return menuId;
    }

    public String getMenuUrl() {
        return menuUrl;
    }

    public String getMenuName() {
        return menuName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public String getAccessType() {
        return accessType;
    }

    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }
}
