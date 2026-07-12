package com.miniproject.dto;

import java.time.LocalDateTime;
import java.util.List;

public class UserResponse {

    private final Long id;
    private final String email;
    private final String name;
    private final List<RoleResponse> roles;
    private final LocalDateTime createdAt;

    public UserResponse(Long id, String email, String name, List<RoleResponse> roles, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.roles = roles;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public List<RoleResponse> getRoles() {
        return roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
