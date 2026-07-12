package com.miniproject.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.dto.MenuResponse;
import com.miniproject.service.MenuService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/my")
    public ApiResponse<List<MenuResponse>> getMyMenus(@AuthenticationPrincipal String email) {
        return ApiResponse.success(menuService.getMyMenus(email));
    }
}
