package com.miniproject.i18n.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "i18n_message_groups", uniqueConstraints = {
        @UniqueConstraint(name = "uk_i18n_message_groups_code", columnNames = "code")
})
public class I18nMessageGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected I18nMessageGroup() {
    }

    public I18nMessageGroup(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }
}
