package com.miniproject.auth.dto;

import com.miniproject.user.dto.UserResponse;

public class AuthResponse {

    private final String accessToken;
    private final String tokenType;
    private final UserResponse user;

    public AuthResponse(String accessToken, UserResponse user) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public UserResponse getUser() {
        return user;
    }
}
