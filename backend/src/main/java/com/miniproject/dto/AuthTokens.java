package com.miniproject.dto;

public class AuthTokens {

    private final String accessToken;
    private final String refreshToken;
    private final UserResponse user;

    public AuthTokens(String accessToken, String refreshToken, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public UserResponse getUser() {
        return user;
    }

    public AuthResponse toResponse() {
        return new AuthResponse(accessToken, user);
    }
}
