package com.miniproject.i18n.domain;

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
@Table(name = "i18n_messages", uniqueConstraints = {
        @UniqueConstraint(name = "uk_i18n_messages_group_code", columnNames = {"group_id", "code"})
})
public class I18nMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private I18nMessageGroup group;

    @Column(nullable = false, length = 150)
    private String code;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected I18nMessage() {
    }

    public I18nMessage(I18nMessageGroup group, String code, String description) {
        this.group = group;
        this.code = code;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public I18nMessageGroup getGroup() {
        return group;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void changeDescription(String description) {
        this.description = description;
    }
}
