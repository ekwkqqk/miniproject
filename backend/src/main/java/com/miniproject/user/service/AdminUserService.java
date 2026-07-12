package com.miniproject.user.service;

import com.miniproject.role.service.RoleService;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.role.domain.Role;
import com.miniproject.user.domain.User;
import com.miniproject.user.domain.UserRepository;
import com.miniproject.role.domain.UserRole;
import com.miniproject.role.domain.UserRoleRepository;
import com.miniproject.role.dto.RoleResponse;
import com.miniproject.user.dto.UpdateUserRolesRequest;
import com.miniproject.user.dto.UserResponse;
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

    public AdminUserService(UserRepository userRepository,
                            UserRoleRepository userRoleRepository,
                            RoleService roleService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleService = roleService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
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

    public UserResponse toUserResponse(User user) {
        List<RoleResponse> roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .map(RoleResponse::from)
                .toList();
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), roles, user.getCreatedAt());
    }
}
