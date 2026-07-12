package com.miniproject.i18n.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.i18n.dto.I18nGroupRequest;
import com.miniproject.i18n.dto.I18nLocaleRequest;
import com.miniproject.i18n.dto.I18nMessageRequest;
import com.miniproject.i18n.service.I18nService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/i18n")
public class AdminI18nController {

    private final I18nService i18nService;

    public AdminI18nController(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @GetMapping("/locales")
    public ApiResponse<List<I18nLocaleRequest.Response>> getLocales() {
        return ApiResponse.success(i18nService.getAllLocales());
    }

    @PostMapping("/locales")
    public ApiResponse<I18nLocaleRequest.Response> createLocale(@Valid @RequestBody I18nLocaleRequest request) {
        return ApiResponse.success(i18nService.createLocale(request), "로케일이 등록되었습니다.");
    }

    @PutMapping("/locales/{id}")
    public ApiResponse<I18nLocaleRequest.Response> updateLocale(@PathVariable Long id,
                                                                @Valid @RequestBody I18nLocaleRequest request) {
        return ApiResponse.success(i18nService.updateLocale(id, request), "로케일이 수정되었습니다.");
    }

    @DeleteMapping("/locales/{id}")
    public ApiResponse<Void> deleteLocale(@PathVariable Long id) {
        i18nService.deleteLocale(id);
        return ApiResponse.success("로케일이 삭제되었습니다.");
    }

    @GetMapping("/groups")
    public ApiResponse<List<I18nGroupRequest.Response>> getGroups() {
        return ApiResponse.success(i18nService.getAllGroups());
    }

    @PostMapping("/groups")
    public ApiResponse<I18nGroupRequest.Response> createGroup(@Valid @RequestBody I18nGroupRequest request) {
        return ApiResponse.success(i18nService.createGroup(request), "메시지 그룹이 등록되었습니다.");
    }

    @PutMapping("/groups/{id}")
    public ApiResponse<I18nGroupRequest.Response> updateGroup(@PathVariable Long id,
                                                              @Valid @RequestBody I18nGroupRequest request) {
        return ApiResponse.success(i18nService.updateGroup(id, request), "메시지 그룹이 수정되었습니다.");
    }

    @DeleteMapping("/groups/{id}")
    public ApiResponse<Void> deleteGroup(@PathVariable Long id) {
        i18nService.deleteGroup(id);
        return ApiResponse.success("메시지 그룹이 삭제되었습니다.");
    }

    @GetMapping("/messages")
    public ApiResponse<List<I18nMessageRequest.Response>> getMessages(
            @RequestParam(required = false) String group) {
        return ApiResponse.success(i18nService.getMessages(group));
    }

    @PostMapping("/messages")
    public ApiResponse<I18nMessageRequest.Response> createMessage(@Valid @RequestBody I18nMessageRequest request) {
        return ApiResponse.success(i18nService.createMessage(request), "메시지가 등록되었습니다.");
    }

    @PutMapping("/messages/{id}")
    public ApiResponse<I18nMessageRequest.Response> updateMessage(@PathVariable Long id,
                                                                  @Valid @RequestBody I18nMessageRequest request) {
        return ApiResponse.success(i18nService.updateMessage(id, request), "메시지가 수정되었습니다.");
    }

    @DeleteMapping("/messages/{id}")
    public ApiResponse<Void> deleteMessage(@PathVariable Long id) {
        i18nService.deleteMessage(id);
        return ApiResponse.success("메시지가 삭제되었습니다.");
    }
}
