package com.miniproject.role.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.role.dto.CreateRoleRequest;
import com.miniproject.role.dto.RoleResponse;
import com.miniproject.role.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

    private final RoleService roleService;

    public AdminRoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getRoles() {
        return ApiResponse.success(roleService.getAllRoles());
    }

    @PostMapping
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.success(roleService.createRole(request), "Role이 생성되었습니다.");
    }

    @DeleteMapping("/{roleId}")
    public ApiResponse<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ApiResponse.success("Role이 삭제되었습니다.");
    }
}
