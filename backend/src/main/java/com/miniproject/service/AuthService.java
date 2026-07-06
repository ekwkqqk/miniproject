package com.miniproject.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.config.JwtTokenProvider;
import com.miniproject.domain.RefreshToken;
import com.miniproject.domain.Role;
import com.miniproject.domain.User;
import com.miniproject.domain.UserRepository;
import com.miniproject.dto.AuthTokens;
import com.miniproject.dto.LoginRequest;
import com.miniproject.dto.RegisterRequest;
import com.miniproject.dto.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenService refreshTokenService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthTokens register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다.");
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                Role.USER
        );
        User savedUser = userRepository.save(user);
        return createAuthTokens(savedUser);
    }

    @Transactional
    public AuthTokens login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        return createAuthTokens(user);
    }

    @Transactional
    public AuthTokens refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();

        refreshTokenService.revoke(refreshToken);
        return createAuthTokens(user);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenService.revokeByToken(refreshTokenValue);
    }

    private AuthTokens createAuthTokens(User user) {
        String accessToken = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthTokens(accessToken, refreshToken, UserResponse.from(user));
    }
}
