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
import com.miniproject.auth.dto.ChangePasswordRequest;
import com.miniproject.auth.dto.LoginRequest;
import com.miniproject.auth.dto.RegisterRequest;
import com.miniproject.role.dto.RoleResponse;
import com.miniproject.settings.domain.SystemSettings;
import com.miniproject.settings.service.SystemSettingsService;
import com.miniproject.user.dto.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RoleService roleService;
    private final SystemSettingsService systemSettingsService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenService refreshTokenService,
                       RoleService roleService,
                       SystemSettingsService systemSettingsService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.roleService = roleService;
        this.systemSettingsService = systemSettingsService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthTokens register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다.");
        }

        systemSettingsService.validatePasswordLength(request.getPassword());
        SystemSettings settings = systemSettingsService.getOrCreate();

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName()
        );
        User savedUser = userRepository.save(user);
        for (String roleCode : settings.getDefaultRoleCodes()) {
            Role defaultRole = roleService.getByCode(roleCode);
            userRoleRepository.save(new UserRole(savedUser, defaultRole));
        }
        return createAuthTokens(savedUser, settings);
    }

    @Transactional
    public AuthTokens login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        assertUserEnabled(user);
        SystemSettings settings = systemSettingsService.getOrCreate();
        assertPasswordNotExpired(user, settings);
        return createAuthTokens(user, settings);
    }

    @Transactional
    public AuthTokens refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();
        assertUserEnabled(user);

        refreshTokenService.revoke(refreshToken);
        SystemSettings settings = systemSettingsService.getOrCreate();
        return createAuthTokens(user, settings);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenService.revokeByToken(refreshTokenValue);
    }

    private AuthTokens createAuthTokens(User user, SystemSettings settings) {
        if (!settings.isAllowMultiLogin()) {
            refreshTokenService.revokeAllActiveSessions(user.getId());
        }
        String accessToken = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthTokens(accessToken, refreshToken, toUserResponse(user));
    }

    private void assertUserEnabled(User user) {
        if (user == null || !user.isEnabled()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "비활성화된 계정입니다. 관리자에게 문의하세요.");
        }
    }

    private void assertPasswordNotExpired(User user, SystemSettings settings) {
        int periodDays = settings.getPasswordChangePeriodDays();
        if (periodDays <= 0) {
            return;
        }
        LocalDateTime changedAt = user.getPasswordChangedAt() != null
                ? user.getPasswordChangedAt()
                : user.getCreatedAt();
        if (changedAt.plusDays(periodDays).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PASSWORD_EXPIRED,
                    "비밀번호 사용 기간이 만료되었습니다. 비밀번호를 변경해주세요.");
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }
        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        systemSettingsService.validatePasswordLength(request.getNewPassword());

        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD,
                        "현재 비밀번호가 틀렸습니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD, "현재 비밀번호가 틀렸습니다.");
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllActiveSessions(user.getId());
    }

    private UserResponse toUserResponse(User user) {
        List<RoleResponse> roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .map(RoleResponse::from)
                .toList();
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), roles, user.getCreatedAt(), user.isEnabled());
    }
}
