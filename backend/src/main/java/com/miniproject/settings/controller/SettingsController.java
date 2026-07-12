package com.miniproject.settings.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.settings.dto.SystemSettingsRequest;
import com.miniproject.settings.service.SystemSettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SystemSettingsService systemSettingsService;

    public SettingsController(SystemSettingsService systemSettingsService) {
        this.systemSettingsService = systemSettingsService;
    }

    @GetMapping("/public")
    public ApiResponse<SystemSettingsRequest.PublicResponse> getPublicSettings() {
        return ApiResponse.success(systemSettingsService.getPublicSettings());
    }
}
