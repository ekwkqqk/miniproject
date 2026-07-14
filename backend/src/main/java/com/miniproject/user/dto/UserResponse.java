package com.miniproject.user.dto;

import com.miniproject.role.dto.RoleResponse;

import java.time.LocalDateTime;
import java.util.List;

public class UserResponse {

    private final Long id;
    private final String email;
    private final String name;
    private final List<RoleResponse> roles;
    private final LocalDateTime createdAt;
    private final boolean enabled;

    public UserResponse(Long id,
                        String email,
                        String name,
                        List<RoleResponse> roles,
                        LocalDateTime createdAt,
                        boolean enabled) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.roles = roles;
        this.createdAt = createdAt;
        this.enabled = enabled;
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

    public boolean isEnabled() {
        return enabled;
    }
}
