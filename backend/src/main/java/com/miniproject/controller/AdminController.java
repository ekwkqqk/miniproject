package com.miniproject.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.dto.UpdateUserRoleRequest;
import com.miniproject.dto.UserResponse;
import com.miniproject.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.success(adminService.getAllUsers());
    }

    @PatchMapping("/users/{userId}/role")
    public ApiResponse<UserResponse> updateUserRole(@PathVariable Long userId,
                                                      @Valid @RequestBody UpdateUserRoleRequest request,
                                                      @AuthenticationPrincipal String adminEmail) {
        return ApiResponse.success(
                adminService.updateUserRole(userId, request, adminEmail),
                "권한 등급이 변경되었습니다."
        );
    }
}
