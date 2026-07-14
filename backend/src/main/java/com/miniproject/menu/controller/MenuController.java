package com.miniproject.menu.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.menu.dto.MenuAccessRequest;
import com.miniproject.menu.dto.MenuResponse;
import com.miniproject.menu.service.MenuAccessLogService;
import com.miniproject.menu.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * 프론트 메뉴 화면 진입 시 호출. 실제 로그 저장은 MenuAccessLogInterceptor가 수행.
     */
    @PostMapping("/access")
    public ApiResponse<Void> reportMenuAccess(@Valid @RequestBody MenuAccessRequest request,
                                              HttpServletRequest httpRequest) {
        String menuUrl = MenuAccessLogService.normalizeMenuUrl(request.getMenuUrl());
        httpRequest.setAttribute(MenuAccessLogService.ATTR_MENU_URL, menuUrl);
        httpRequest.setAttribute(MenuAccessLogService.ATTR_ACCESS_TYPE, MenuAccessLogService.ACCESS_TYPE_MENU);
        return ApiResponse.success("메뉴 접근이 기록되었습니다.");
    }
}
