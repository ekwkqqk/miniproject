package com.miniproject.file.dto;

import com.miniproject.file.domain.StoredFile;

import java.time.LocalDateTime;

public record FileResponse(
        Long id,
        String originalName,
        String contentType,
        long sizeBytes,
        String uploadedByEmail,
        LocalDateTime createdAt
) {
    public static FileResponse from(StoredFile file) {
        return new FileResponse(
                file.getId(),
                file.getOriginalName(),
                file.getContentType(),
                file.getSizeBytes(),
                file.getUploadedByEmail(),
                file.getCreatedAt()
        );
    }
}
