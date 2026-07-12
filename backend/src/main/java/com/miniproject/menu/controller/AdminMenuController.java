package com.miniproject.menu.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.menu.dto.MenuReorderRequest;
import com.miniproject.menu.dto.MenuRequest;
import com.miniproject.menu.dto.MenuResponse;
import com.miniproject.menu.service.MenuService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/menus")
public class AdminMenuController {

    private final MenuService menuService;

    public AdminMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ApiResponse<List<MenuResponse>> getMenus() {
        return ApiResponse.success(menuService.getAllMenus());
    }

    @PostMapping
    public ApiResponse<MenuResponse> createMenu(@Valid @RequestBody MenuRequest request) {
        return ApiResponse.success(menuService.createMenu(request), "메뉴가 등록되었습니다.");
    }

    @PutMapping("/reorder")
    public ApiResponse<List<MenuResponse>> reorderMenu(@Valid @RequestBody MenuReorderRequest request) {
        return ApiResponse.success(menuService.reorderMenu(request), "메뉴 순서가 변경되었습니다.");
    }

    @PutMapping("/{menuId}")
    public ApiResponse<MenuResponse> updateMenu(@PathVariable Long menuId,
                                                @Valid @RequestBody MenuRequest request) {
        return ApiResponse.success(menuService.updateMenu(menuId, request), "메뉴가 수정되었습니다.");
    }

    @DeleteMapping("/{menuId}")
    public ApiResponse<Void> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return ApiResponse.success("메뉴가 삭제되었습니다.");
    }
}
