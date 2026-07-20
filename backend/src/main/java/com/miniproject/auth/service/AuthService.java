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
import com.miniproject.auth.dto.ExternalIdentity;
import com.miniproject.auth.dto.LoginRequest;
import com.miniproject.auth.dto.RegisterRequest;
import com.miniproject.role.dto.RoleResponse;
import com.miniproject.settings.domain.SystemSettings;
import com.miniproject.settings.service.SystemSettingsService;
import com.miniproject.user.dto.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private final LoginAttemptService loginAttemptService;

    public AuthService(UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenService refreshTokenService,
                       RoleService roleService,
                       SystemSettingsService systemSettingsService,
                       AuthenticationManager authenticationManager,
                       LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.roleService = roleService;
        this.systemSettingsService = systemSettingsService;
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
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
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (DisabledException ex) {
            throw ex;
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            if (loginAttemptService.registerFailedLoginAttempt(request.getEmail())) {
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED,
                        "로그인 실패 횟수 초과로 계정이 잠겼습니다. 관리자에게 문의하세요.");
            }
            throw ex;
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        assertUserEnabled(user);
        loginAttemptService.resetFailedLoginAttempts(user.getId());

        SystemSettings settings = systemSettingsService.getOrCreate();
        assertPasswordNotExpired(user, settings);
        return createAuthTokens(user, settings);
    }

    /**
     * SSO/OAuth/OIDC 등으로 IdP 검증을 마친 외부 신원으로 앱 인가(세션)를 수립한다.
     * <p>
     * 흐름:
     * <ol>
     *   <li>이메일로 로컬 사용자를 찾거나 JIT 프로비저닝</li>
     *   <li>활성 계정 확인</li>
     *   <li>(선택) IdP Role → 로컬 Role 동기화</li>
     *   <li>앱 JWT + refresh token 발급</li>
     * </ol>
     * 이후 API 인가·메뉴 권한은 기존과 동일하게 JWT 필터 + {@code MenuService.getMyMenus} 경로를 탄다.
     *
     * <pre>{@code
     * // OAuth2/OIDC SuccessHandler 예시
     * ExternalIdentity identity = ExternalIdentity.of(email, name, subject)
     *         .withRoleCodes(idpRoles, false);
     * AuthTokens tokens = authService.authorizeExternalIdentity(identity);
     * refreshTokenCookieService.setRefreshTokenCookie(response, tokens.getRefreshToken());
     * return tokens.toResponse();
     * }</pre>
     */
    @Transactional
    public AuthTokens authorizeExternalIdentity(ExternalIdentity identity) {
        if (identity == null || identity.getEmail() == null || identity.getEmail().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "외부 신원에 이메일이 없습니다.");
        }

        String email = identity.getEmail().trim();
        SystemSettings settings = systemSettingsService.getOrCreate();

        User user = userRepository.findByEmail(email).orElse(null);
        boolean created = false;
        if (user == null) {
            user = provisionExternalUser(email, identity);
            created = true;
        } else {
            updateExternalUserProfile(user, identity);
        }

        assertUserEnabled(user);

        if (created || identity.isSyncRoles()) {
            assignRolesFromExternal(user, identity, settings);
        }

        loginAttemptService.resetFailedLoginAttempts(user.getId());
        return createAuthTokens(user, settings);
    }

    /**
     * 이미 로컬 {@link User}가 확보된 경우(커스텀 SSO 매핑 후) 앱 세션만 발급한다.
     * 비밀번호 만료 검사는 하지 않는다.
     */
    @Transactional
    public AuthTokens issueSession(User user) {
        assertUserEnabled(user);
        SystemSettings settings = systemSettingsService.getOrCreate();
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

    private User provisionExternalUser(String email, ExternalIdentity identity) {
        String displayName = resolveDisplayName(identity, email);
        // 로컬 비밀번호 로그인 불가에 가깝게, 추측 불가능한 해시 저장
        String unusablePassword = passwordEncoder.encode("SSO:" + UUID.randomUUID());
        User user = new User(email, unusablePassword, displayName);
        return userRepository.save(user);
    }

    private void updateExternalUserProfile(User user, ExternalIdentity identity) {
        String nextName = identity.getName() == null ? null : identity.getName().trim();
        if (nextName != null && !nextName.isEmpty() && !nextName.equals(user.getName())) {
            user.setName(nextName);
            userRepository.save(user);
        }
    }

    private void assignRolesFromExternal(User user, ExternalIdentity identity, SystemSettings settings) {
        Set<String> codes = new LinkedHashSet<>();
        if (identity.getRoleCodes() != null) {
            for (String code : identity.getRoleCodes()) {
                if (code != null && !code.isBlank()) {
                    codes.add(code.trim());
                }
            }
        }
        if (codes.isEmpty()) {
            codes.addAll(settings.getDefaultRoleCodes());
        }

        userRoleRepository.deleteByUserId(user.getId());
        for (String code : codes) {
            Role role = roleService.getByCode(code);
            userRoleRepository.save(new UserRole(user, role));
        }
    }

    private static String resolveDisplayName(ExternalIdentity identity, String email) {
        if (identity.getName() != null && !identity.getName().isBlank()) {
            return identity.getName().trim();
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
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
