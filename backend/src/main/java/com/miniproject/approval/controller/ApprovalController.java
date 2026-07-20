package com.miniproject.approval.controller;

import com.miniproject.approval.dto.ApprovalActionRequest;
import com.miniproject.approval.dto.ApprovalDocumentRequest;
import com.miniproject.approval.dto.ApprovalDocumentResponse;
import com.miniproject.approval.service.ApprovalService;
import com.miniproject.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/documents")
    public ApiResponse<Map<String, Object>> list(
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "related") String box,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(approvalService.search(email, box, status, keyword, page, size));
    }

    @GetMapping("/inbox")
    public ApiResponse<Map<String, Object>> inbox(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(approvalService.search(email, "inbox", null, keyword, page, size));
    }

    @GetMapping("/notices")
    public ApiResponse<Map<String, Object>> notices(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(approvalService.search(email, "notices", status, keyword, page, size));
    }

    @GetMapping("/badges")
    public ApiResponse<Map<String, Long>> badges(@AuthenticationPrincipal String email) {
        return ApiResponse.success(approvalService.badges(email));
    }

    @GetMapping("/documents/{id}")
    public ApiResponse<ApprovalDocumentResponse> get(
            @AuthenticationPrincipal String email,
            @PathVariable Long id
    ) {
        return ApiResponse.success(approvalService.get(email, id));
    }

    @PostMapping("/documents")
    public ApiResponse<ApprovalDocumentResponse> create(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ApprovalDocumentRequest request
    ) {
        return ApiResponse.success(approvalService.create(email, request), "임시저장되었습니다.");
    }

    @PutMapping("/documents/{id}")
    public ApiResponse<ApprovalDocumentResponse> update(
            @AuthenticationPrincipal String email,
            @PathVariable Long id,
            @Valid @RequestBody ApprovalDocumentRequest request
    ) {
        return ApiResponse.success(approvalService.update(email, id, request), "저장되었습니다.");
    }

    @DeleteMapping("/documents/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal String email,
            @PathVariable Long id
    ) {
        approvalService.delete(email, id);
        return ApiResponse.success("삭제되었습니다.");
    }

    @PostMapping("/documents/{id}/submit")
    public ApiResponse<ApprovalDocumentResponse> submit(
            @AuthenticationPrincipal String email,
            @PathVariable Long id
    ) {
        return ApiResponse.success(approvalService.submit(email, id), "상신되었습니다.");
    }

    @PostMapping("/documents/{id}/recall")
    public ApiResponse<ApprovalDocumentResponse> recall(
            @AuthenticationPrincipal String email,
            @PathVariable Long id
    ) {
        return ApiResponse.success(approvalService.recall(email, id), "회수되었습니다.");
    }

    @PostMapping("/documents/{id}/approve")
    public ApiResponse<ApprovalDocumentResponse> approve(
            @AuthenticationPrincipal String email,
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionRequest request
    ) {
        return ApiResponse.success(approvalService.approve(email, id, request), "승인되었습니다.");
    }

    @PostMapping("/documents/{id}/reject")
    public ApiResponse<ApprovalDocumentResponse> reject(
            @AuthenticationPrincipal String email,
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionRequest request
    ) {
        return ApiResponse.success(approvalService.reject(email, id, request), "반려되었습니다.");
    }

    @PostMapping("/documents/{id}/acknowledge")
    public ApiResponse<ApprovalDocumentResponse> acknowledge(
            @AuthenticationPrincipal String email,
            @PathVariable Long id,
            @Valid @RequestBody ApprovalActionRequest request
    ) {
        return ApiResponse.success(approvalService.acknowledge(email, id, request), "확인되었습니다.");
    }
}
