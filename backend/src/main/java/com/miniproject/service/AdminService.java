package com.miniproject.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.domain.Role;
import com.miniproject.domain.User;
import com.miniproject.domain.UserRepository;
import com.miniproject.dto.UpdateUserRoleRequest;
import com.miniproject.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, UpdateUserRoleRequest request, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getEmail().equals(adminEmail)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 권한은 변경할 수 없습니다.");
        }

        user.changeRole(request.getRole());
        return UserResponse.from(user);
    }
}
