package com.miniproject.auth.service;

import com.miniproject.role.service.RoleService;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.config.JwtTokenProvider;
import com.miniproject.auth.domain.RefreshToken;
import com.miniproject.role.domain.Role;
import com.miniproject.user.domain.User;
import com.miniproject.user.domain.UserRepository;
import com.miniproject.role.domain.UserRole;
import com.miniproject.role.domain.UserRoleRepository;
import com.miniproject.auth.dto.AuthTokens;
import com.miniproject.auth.dto.LoginRequest;
import com.miniproject.auth.dto.RegisterRequest;
import com.miniproject.role.dto.RoleResponse;
import com.miniproject.user.dto.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenService refreshTokenService,
                       RoleService roleService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.roleService = roleService;
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
                request.getName()
        );
        User savedUser = userRepository.save(user);
        Role defaultRole = roleService.getByCode(RoleService.USER_CODE);
        userRoleRepository.save(new UserRole(savedUser, defaultRole));
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
        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthTokens(accessToken, refreshToken, toUserResponse(user));
    }

    private UserResponse toUserResponse(User user) {
        List<RoleResponse> roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .map(RoleResponse::from)
                .toList();
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), roles, user.getCreatedAt());
    }
}
