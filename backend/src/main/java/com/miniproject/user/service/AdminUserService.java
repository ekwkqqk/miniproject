package com.miniproject.user.service;

import com.miniproject.auth.service.RefreshTokenService;
import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.role.domain.Role;
import com.miniproject.role.domain.UserRole;
import com.miniproject.role.domain.UserRoleRepository;
import com.miniproject.role.dto.RoleResponse;
import com.miniproject.role.service.RoleService;
import com.miniproject.settings.domain.SystemSettings;
import com.miniproject.settings.service.SystemSettingsService;
import com.miniproject.user.domain.User;
import com.miniproject.user.domain.UserRepository;
import com.miniproject.user.dto.CreateUserRequest;
import com.miniproject.user.dto.UpdateUserEnabledRequest;
import com.miniproject.user.dto.UpdateUserRolesRequest;
import com.miniproject.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleService roleService;
    private final RefreshTokenService refreshTokenService;
    private final SystemSettingsService systemSettingsService;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository,
                            UserRoleRepository userRoleRepository,
                            RoleService roleService,
                            RefreshTokenService refreshTokenService,
                            SystemSettingsService systemSettingsService,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleService = roleService;
        this.refreshTokenService = refreshTokenService;
        this.systemSettingsService = systemSettingsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String email = request.getEmail().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다.");
        }

        systemSettingsService.validatePasswordLength(request.getPassword());
        SystemSettings settings = systemSettingsService.getOrCreate();

        User user = new User(
                email,
                passwordEncoder.encode(request.getPassword()),
                request.getName().trim()
        );
        user.changeEnabled(Boolean.TRUE.equals(request.getEnabled()));
        User saved = userRepository.save(user);

        List<String> roleCodes = settings.getDefaultRoleCodes();
        if (roleCodes.isEmpty()) {
            roleCodes = List.of(RoleService.USER_CODE);
        }
        for (String roleCode : roleCodes) {
            Role role = roleService.getByCode(roleCode);
            userRoleRepository.save(new UserRole(saved, role));
        }
        return toUserResponse(saved);
    }

    @Transactional
    public UserResponse updateUserRoles(Long userId, UpdateUserRolesRequest request, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getEmail().equals(adminEmail)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 Role은 변경할 수 없습니다.");
        }

        Set<Long> roleIds = new HashSet<>(request.getRoleIds());
        if (roleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Role을 하나 이상 지정해주세요.");
        }

        userRoleRepository.deleteByUserId(userId);
        for (Long roleId : roleIds) {
            Role role = roleService.getRequiredRole(roleId);
            userRoleRepository.save(new UserRole(user, role));
        }
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUserEnabled(Long userId, UpdateUserEnabledRequest request, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getEmail().equals(adminEmail)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 계정은 비활성화할 수 없습니다.");
        }

        user.changeEnabled(Boolean.TRUE.equals(request.getEnabled()));
        userRepository.save(user);
        if (!user.isEnabled()) {
            refreshTokenService.revokeAllActiveSessions(user.getId());
        }
        return toUserResponse(user);
    }

    public UserResponse toUserResponse(User user) {
        List<RoleResponse> roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .map(RoleResponse::from)
                .toList();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                roles,
                user.getCreatedAt(),
                user.isEnabled()
        );
    }
}
