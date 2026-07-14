package com.miniproject.settings.domain;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SystemSettings {

    public static final long SINGLETON_ID = 1L;

    private Long id = SINGLETON_ID;

    /** 테마 primary 색상 (hex) */
    private String themePrimaryColor;

    /** 비밀번호 변경 주기(일). 0이면 미사용 */
    private int passwordChangePeriodDays;

    /** 비밀번호 최소 길이 */
    private int passwordMinLength;

    /** 회원가입 시 부여할 Role 코드들 (쉼표 구분) */
    private String defaultRoleCodes;

    /** true면 동일 계정 다중 로그인 허용 */
    private boolean allowMultiLogin;

    /** 로그인 실패 시 계정 잠금 기준 횟수. 0이면 미사용 */
    private int maxFailedLoginAttempts;

    private LocalDateTime updatedAt;

    protected SystemSettings() {
    }

    public static SystemSettings defaults() {
        SystemSettings settings = new SystemSettings();
        settings.id = SINGLETON_ID;
        settings.themePrimaryColor = "#409EFF";
        settings.passwordChangePeriodDays = 0;
        settings.passwordMinLength = 6;
        settings.defaultRoleCodes = "USER";
        settings.allowMultiLogin = true;
        settings.maxFailedLoginAttempts = 0;
        settings.updatedAt = LocalDateTime.now();
        return settings;
    }

    public void update(String themePrimaryColor,
                       int passwordChangePeriodDays,
                       int passwordMinLength,
                       List<String> defaultRoleCodes,
                       boolean allowMultiLogin,
                       int maxFailedLoginAttempts) {
        this.themePrimaryColor = themePrimaryColor;
        this.passwordChangePeriodDays = passwordChangePeriodDays;
        this.passwordMinLength = passwordMinLength;
        this.defaultRoleCodes = joinRoleCodes(defaultRoleCodes);
        this.allowMultiLogin = allowMultiLogin;
        this.maxFailedLoginAttempts = maxFailedLoginAttempts;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getThemePrimaryColor() {
        return themePrimaryColor;
    }

    public void setThemePrimaryColor(String themePrimaryColor) {
        this.themePrimaryColor = themePrimaryColor;
    }

    public int getPasswordChangePeriodDays() {
        return passwordChangePeriodDays;
    }

    public void setPasswordChangePeriodDays(int passwordChangePeriodDays) {
        this.passwordChangePeriodDays = passwordChangePeriodDays;
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public String getDefaultRoleCodesRaw() {
        return defaultRoleCodes;
    }

    public void setDefaultRoleCodes(String defaultRoleCodes) {
        this.defaultRoleCodes = defaultRoleCodes;
    }

    public List<String> getDefaultRoleCodes() {
        return parseRoleCodes(defaultRoleCodes);
    }

    public boolean isAllowMultiLogin() {
        return allowMultiLogin;
    }

    public void setAllowMultiLogin(boolean allowMultiLogin) {
        this.allowMultiLogin = allowMultiLogin;
    }

    public int getMaxFailedLoginAttempts() {
        return maxFailedLoginAttempts;
    }

    public void setMaxFailedLoginAttempts(int maxFailedLoginAttempts) {
        this.maxFailedLoginAttempts = maxFailedLoginAttempts;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static List<String> parseRoleCodes(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.toList());
    }

    public static String joinRoleCodes(List<String> codes) {
        return codes.stream()
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .collect(Collectors.joining(","));
    }
}
