package com.miniproject.domain;

public enum Role {
    USER("일반사용자"),
    SPECIAL_USER("특별사용자"),
    SYSTEM_ADMIN("시스템관리자");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
