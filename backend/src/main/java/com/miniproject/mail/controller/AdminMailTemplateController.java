package com.miniproject.mail.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.mail.dto.MailTemplateRequest;
import com.miniproject.mail.service.MailTemplateService;
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
@RequestMapping("/api/admin/mail/templates")
public class AdminMailTemplateController {

    private final MailTemplateService mailTemplateService;

    public AdminMailTemplateController(MailTemplateService mailTemplateService) {
        this.mailTemplateService = mailTemplateService;
    }

    @GetMapping
    public ApiResponse<List<MailTemplateRequest.Response>> getTemplates() {
        return ApiResponse.success(mailTemplateService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<MailTemplateRequest.Response> getTemplate(@PathVariable Long id) {
        return ApiResponse.success(mailTemplateService.getById(id));
    }

    @PostMapping
    public ApiResponse<MailTemplateRequest.Response> create(@Valid @RequestBody MailTemplateRequest request) {
        return ApiResponse.success(mailTemplateService.create(request), "메일 템플릿이 등록되었습니다.");
    }

    @PutMapping("/{id}")
    public ApiResponse<MailTemplateRequest.Response> update(@PathVariable Long id,
                                                            @Valid @RequestBody MailTemplateRequest request) {
        return ApiResponse.success(mailTemplateService.update(id, request), "메일 템플릿이 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mailTemplateService.delete(id);
        return ApiResponse.success("메일 템플릿이 삭제되었습니다.");
    }
}
