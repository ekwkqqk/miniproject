package com.miniproject.settings.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.settings.dto.SystemSettingsRequest;
import com.miniproject.settings.service.SystemSettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    private final SystemSettingsService systemSettingsService;

    public AdminSettingsController(SystemSettingsService systemSettingsService) {
        this.systemSettingsService = systemSettingsService;
    }

    @GetMapping
    public ApiResponse<SystemSettingsRequest.Response> getSettings() {
        return ApiResponse.success(systemSettingsService.getSettings());
    }

    @PutMapping
    public ApiResponse<SystemSettingsRequest.Response> updateSettings(
            @Valid @RequestBody SystemSettingsRequest request) {
        return ApiResponse.success(systemSettingsService.update(request), "시스템 설정이 저장되었습니다.");
    }
}
