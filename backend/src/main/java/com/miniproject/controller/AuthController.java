package com.miniproject.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.config.RefreshTokenCookieService;
import com.miniproject.dto.AuthResponse;
import com.miniproject.dto.AuthTokens;
import com.miniproject.dto.LoginRequest;
import com.miniproject.dto.RegisterRequest;
import com.miniproject.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    public AuthController(AuthService authService, RefreshTokenCookieService refreshTokenCookieService) {
        this.authService = authService;
        this.refreshTokenCookieService = refreshTokenCookieService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                              HttpServletResponse response) {
        return ApiResponse.success(issueTokens(authService.register(request), response), "회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                           HttpServletResponse response) {
        return ApiResponse.success(issueTokens(authService.login(request), response), "로그인되었습니다.");
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = refreshTokenCookieService.getRefreshToken(request)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "refresh token이 없습니다."));
        return ApiResponse.success(issueTokens(authService.refresh(refreshToken), response), "토큰이 갱신되었습니다.");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        refreshTokenCookieService.getRefreshToken(request).ifPresent(authService::logout);
        refreshTokenCookieService.clearRefreshTokenCookie(response);
        return ApiResponse.success("로그아웃되었습니다.");
    }

    private AuthResponse issueTokens(AuthTokens tokens, HttpServletResponse response) {
        refreshTokenCookieService.setRefreshTokenCookie(response, tokens.getRefreshToken());
        return tokens.toResponse();
    }
}
