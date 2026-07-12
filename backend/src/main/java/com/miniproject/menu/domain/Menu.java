package com.miniproject.menu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "menus", uniqueConstraints = {
        @UniqueConstraint(name = "uk_menus_url", columnNames = "url")
})
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    @Column(nullable = false, length = 100)
    private String name;

    /** 다국어 메시지 키 (group.code). null이면 name 사용 */
    @Column(length = 200)
    private String nameI18nKey;

    @Column(length = 200)
    private String url;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Menu() {
    }

    public Menu(String name, String url, int sortOrder, Menu parent) {
        this.name = name;
        this.url = url;
        this.sortOrder = sortOrder;
        this.parent = parent;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Menu getParent() {
        return parent;
    }

    public Long getParentId() {
        return parent == null ? null : parent.getId();
    }

    public String getName() {
        return name;
    }

    public String getNameI18nKey() {
        return nameI18nKey;
    }

    public String getUrl() {
        return url;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isFolder() {
        return url == null || url.isBlank();
    }

    public void changeParent(Menu parent) {
        this.parent = parent;
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
