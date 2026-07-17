package com.miniproject.user.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.user.dto.CreateUserRequest;
import com.miniproject.user.dto.UpdateUserRequest;
import com.miniproject.user.dto.UpdateUserEnabledRequest;
import com.miniproject.user.dto.UpdateUserRolesRequest;
import com.miniproject.user.dto.UserResponse;
import com.miniproject.user.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.success(adminUserService.getAllUsers());
    }

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(adminUserService.createUser(request), "사용자가 추가되었습니다.");
    }

    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long userId,
                                                @Valid @RequestBody UpdateUserRequest request,
                                                @AuthenticationPrincipal String adminEmail) {
        return ApiResponse.success(
                adminUserService.updateUser(userId, request, adminEmail),
                "사용자 정보가 수정되었습니다."
        );
    }

    @PutMapping("/{userId}/roles")
    public ApiResponse<UserResponse> updateUserRoles(@PathVariable Long userId,
                                                     @Valid @RequestBody UpdateUserRolesRequest request,
                                                     @AuthenticationPrincipal String adminEmail) {
        return ApiResponse.success(
                adminUserService.updateUserRoles(userId, request, adminEmail),
                "사용자 Role이 변경되었습니다."
        );
    }

    @PutMapping("/{userId}/enabled")
    public ApiResponse<UserResponse> updateUserEnabled(@PathVariable Long userId,
                                                       @Valid @RequestBody UpdateUserEnabledRequest request,
                                                       @AuthenticationPrincipal String adminEmail) {
        boolean enabled = Boolean.TRUE.equals(request.getEnabled());
        return ApiResponse.success(
                adminUserService.updateUserEnabled(userId, request, adminEmail),
                enabled ? "계정이 활성화되었습니다." : "계정이 비활성화되었습니다."
        );
    }
}
