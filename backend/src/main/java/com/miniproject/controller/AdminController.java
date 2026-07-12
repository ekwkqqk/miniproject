package com.miniproject.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.dto.CreateRoleRequest;
import com.miniproject.dto.MenuRequest;
import com.miniproject.dto.MenuResponse;
import com.miniproject.dto.RoleResponse;
import com.miniproject.dto.UpdateUserRolesRequest;
import com.miniproject.dto.UserResponse;
import com.miniproject.service.AdminUserService;
import com.miniproject.service.MenuService;
import com.miniproject.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RoleService roleService;
    private final MenuService menuService;
    private final AdminUserService adminUserService;

    public AdminController(RoleService roleService, MenuService menuService, AdminUserService adminUserService) {
        this.roleService = roleService;
        this.menuService = menuService;
        this.adminUserService = adminUserService;
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> getRoles() {
        return ApiResponse.success(roleService.getAllRoles());
    }

    @PostMapping("/roles")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.success(roleService.createRole(request), "Role이 생성되었습니다.");
    }

    @DeleteMapping("/roles/{roleId}")
    public ApiResponse<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ApiResponse.success("Role이 삭제되었습니다.");
    }

    @GetMapping("/menus")
    public ApiResponse<List<MenuResponse>> getMenus() {
        return ApiResponse.success(menuService.getAllMenus());
    }

    @PostMapping("/menus")
    public ApiResponse<MenuResponse> createMenu(@Valid @RequestBody MenuRequest request) {
        return ApiResponse.success(menuService.createMenu(request), "메뉴가 등록되었습니다.");
    }

    @PutMapping("/menus/{menuId}")
    public ApiResponse<MenuResponse> updateMenu(@PathVariable Long menuId,
                                                @Valid @RequestBody MenuRequest request) {
        return ApiResponse.success(menuService.updateMenu(menuId, request), "메뉴가 수정되었습니다.");
    }

    @DeleteMapping("/menus/{menuId}")
    public ApiResponse<Void> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return ApiResponse.success("메뉴가 삭제되었습니다.");
    }

    @GetMapping("/users")
    public ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.success(adminUserService.getAllUsers());
    }

    @PutMapping("/users/{userId}/roles")
    public ApiResponse<UserResponse> updateUserRoles(@PathVariable Long userId,
                                                     @Valid @RequestBody UpdateUserRolesRequest request,
                                                     @AuthenticationPrincipal String adminEmail) {
        return ApiResponse.success(
                adminUserService.updateUserRoles(userId, request, adminEmail),
                "사용자 Role이 변경되었습니다."
        );
    }
}
