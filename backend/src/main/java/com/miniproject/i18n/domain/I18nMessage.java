package com.miniproject.i18n.domain;

import java.time.LocalDateTime;

public class I18nMessage {

    private Long id;
    private Long groupId;
    private I18nMessageGroup group;
    private String code;
    private String description;
    private LocalDateTime createdAt;

    protected I18nMessage() {
    }

    public I18nMessage(I18nMessageGroup group, String code, String description) {
        this.group = group;
        this.groupId = group != null ? group.getId() : null;
        this.code = code;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public I18nMessageGroup getGroup() {
        return group;
    }

    public void setGroup(I18nMessageGroup group) {
        this.group = group;
        this.groupId = group != null ? group.getId() : this.groupId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void changeDescription(String description) {
        this.description = description;
    }
}
