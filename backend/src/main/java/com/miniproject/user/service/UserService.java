package com.miniproject.user.service;

import com.miniproject.role.service.RoleService;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.user.domain.User;
import com.miniproject.user.domain.UserRepository;
import com.miniproject.role.domain.UserRoleRepository;
import com.miniproject.role.dto.RoleResponse;
import com.miniproject.user.dto.UserResponse;
import com.miniproject.user.dto.UserSummaryResponse;
import com.miniproject.role.domain.UserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        List<RoleResponse> roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(UserRole::getRole)
                .map(RoleResponse::from)
                .toList();
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), roles, user.getCreatedAt(), user.isEnabled());
    }

    public List<UserSummaryResponse> searchEnabledUsers(String email, String keyword, int limit) {
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다."));
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return userRepository.searchEnabled(kw, me.getId(), safeLimit).stream()
                .map(u -> new UserSummaryResponse(u.getId(), u.getEmail(), u.getName()))
                .toList();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        String[] roleCodes = userRoleRepository.findRoleCodesByUserEmail(email).toArray(String[]::new);
        if (roleCodes.length == 0) {
            roleCodes = new String[]{RoleService.USER_CODE};
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .roles(roleCodes)
                .build();
    }
}
