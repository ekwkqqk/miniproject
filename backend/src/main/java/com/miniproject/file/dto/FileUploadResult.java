package com.miniproject.file.dto;

import java.util.List;

public record FileUploadResult(
        Long fileGroupId,
        List<Long> fileIds,
        List<FileResponse> files
) {
}
