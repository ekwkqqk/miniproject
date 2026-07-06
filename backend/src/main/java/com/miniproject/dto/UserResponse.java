package com.miniproject.dto;

import com.miniproject.domain.Role;
import com.miniproject.domain.User;

import java.time.LocalDateTime;

public class UserResponse {

    private final Long id;
    private final String email;
    private final String name;
    private final Role role;
    private final String roleLabel;
    private final LocalDateTime createdAt;

    public UserResponse(Long id, String email, String name, Role role, String roleLabel, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.roleLabel = roleLabel;
        this.createdAt = createdAt;
    }

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getRole().getDisplayName(),
                user.getCreatedAt()
        );
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

    public Role getRole() {
        return role;
    }

    public String getRoleLabel() {
        return roleLabel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
