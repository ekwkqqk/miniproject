package com.miniproject.menu.domain;

import java.time.LocalDateTime;

public class Menu {

    private Long id;
    private Long parentId;
    private Menu parent;
    private String name;
    /** 다국어 메시지 키 (group.code). null이면 name 사용 */
    private String nameI18nKey;
    private String url;
    private int sortOrder;
    private LocalDateTime createdAt;

    protected Menu() {
    }

    public Menu(String name, String url, int sortOrder, Menu parent) {
        this.name = name;
        this.url = url;
        this.sortOrder = sortOrder;
        this.parent = parent;
        this.parentId = parent == null ? null : parent.getId();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Menu getParent() {
        return parent;
    }

    public void setParent(Menu parent) {
        this.parent = parent;
        this.parentId = parent == null ? null : parent.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameI18nKey() {
        return nameI18nKey;
    }

    public void setNameI18nKey(String nameI18nKey) {
        this.nameI18nKey = nameI18nKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public boolean isFolder() {
        return url == null || url.isBlank();
    }

    public void changeParent(Menu parent) {
        this.parent = parent;
        this.parentId = parent == null ? null : parent.getId();
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeNameI18nKey(String nameI18nKey) {
        this.nameI18nKey = nameI18nKey;
    }

    public void changeUrl(String url) {
        this.url = url;
    }

    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
