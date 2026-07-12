package com.miniproject.dto;

import com.miniproject.domain.Role;

import java.time.LocalDateTime;

public class RoleResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final String description;
    private final LocalDateTime createdAt;

    public RoleResponse(Long id, String code, String name, String description, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt()
        );
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
}
