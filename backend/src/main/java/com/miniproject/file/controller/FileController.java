package com.miniproject.file.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.file.dto.FileResponse;
import com.miniproject.file.dto.FileUploadResult;
import com.miniproject.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Files")
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "파일 업로드 (multipart) — 동일 요청의 파일은 같은 fileGroupId")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResult> upload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "accept", required = false) String accept,
            @RequestParam(value = "limit", required = false) Integer limit,
            @AuthenticationPrincipal String email
    ) {
        return ApiResponse.success(fileService.upload(files, accept, limit, email));
    }

    @Operation(summary = "파일 그룹으로 목록 조회")
    @GetMapping("/groups/{fileGroupId}")
    public ApiResponse<List<FileResponse>> getByGroup(@PathVariable Long fileGroupId) {
        return ApiResponse.success(fileService.getByFileGroupId(fileGroupId));
    }

    @Operation(summary = "파일 메타데이터 조회")
    @GetMapping("/{id}")
    public ApiResponse<FileResponse> get(@PathVariable Long id) {
        return ApiResponse.success(fileService.get(id));
    }

    @Operation(summary = "파일 다운로드")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        var meta = fileService.requireFile(id);
        Resource resource = fileService.loadResource(id);
        String encoded = UriUtils.encode(meta.getOriginalName(), StandardCharsets.UTF_8);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (meta.getContentType() != null && !meta.getContentType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(meta.getContentType());
            } catch (Exception ignored) {
                // keep octet-stream
            }
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(resource);
    }

    @Operation(summary = "파일 삭제")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return ApiResponse.success(null, "파일이 삭제되었습니다.");
    }
}
