package com.miniproject.i18n.domain;

import java.time.LocalDateTime;

public class I18nLocale {

    private Long id;
    private String code;
    private String name;
    private boolean enabled;
    private int sortOrder;
    private LocalDateTime createdAt;

    protected I18nLocale() {
    }

    public I18nLocale(String code, String name, boolean enabled, int sortOrder) {
        this.code = code;
        this.name = name;
        this.enabled = enabled;
        this.sortOrder = sortOrder;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
