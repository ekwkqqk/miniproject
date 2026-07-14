package com.miniproject.menu.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.menu.service.MenuAccessLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/menu-access-logs")
public class AdminMenuAccessLogController {

    private final MenuAccessLogService menuAccessLogService;

    public AdminMenuAccessLogController(MenuAccessLogService menuAccessLogService) {
        this.menuAccessLogService = menuAccessLogService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.success(menuAccessLogService.getRecentLogs(page, size));
    }
}
