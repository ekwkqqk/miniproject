package com.miniproject.i18n.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.i18n.dto.I18nLocaleRequest;
import com.miniproject.i18n.dto.I18nResolveRequest;
import com.miniproject.i18n.service.I18nService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/i18n")
public class I18nController {

    private final I18nService i18nService;

    public I18nController(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @GetMapping("/locales")
    public ApiResponse<List<I18nLocaleRequest.Response>> getLocales() {
        return ApiResponse.success(i18nService.getEnabledLocales());
    }

    @GetMapping("/messages")
    public ApiResponse<Map<String, String>> getMessages(
            @RequestParam String locale,
            @RequestParam(required = false) String group) {
        return ApiResponse.success(i18nService.getMessageBundle(locale, group));
    }

    @PostMapping("/resolve")
    public ApiResponse<Map<String, Object>> resolve(@Valid @RequestBody I18nResolveRequest request) {
        return ApiResponse.success(i18nService.resolve(request));
    }
}
