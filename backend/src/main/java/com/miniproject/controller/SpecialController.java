package com.miniproject.controller;

import com.miniproject.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/special")
@PreAuthorize("hasAnyRole('SPECIAL_USER', 'SYSTEM_ADMIN')")
public class SpecialController {

    @GetMapping("/info")
    public ApiResponse<Map<String, String>> getSpecialInfo() {
        return ApiResponse.success(Map.of(
                "title", "특별 사용자 전용",
                "message", "특별사용자 또는 시스템관리자만 접근할 수 있는 영역입니다."
        ));
    }
}
