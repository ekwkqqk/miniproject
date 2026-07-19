package com.miniproject.common.excel;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 엑셀 파일 HTTP 응답 공통 헬퍼.
 */
public final class ExcelResponses {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private ExcelResponses() {
    }

    public static ResponseEntity<byte[]> xlsx(byte[] body, String filenamePrefix) {
        String prefix = (filenamePrefix == null || filenamePrefix.isBlank()) ? "export" : filenamePrefix.trim();
        String filename = prefix + "-" + LocalDateTime.now().format(FILE_TS) + ".xlsx";
        return xlsxWithFilename(body, filename);
    }

    public static ResponseEntity<byte[]> xlsxWithFilename(byte[] body, String filename) {
        byte[] safeBody = body == null ? new byte[0] : body;
        String safeName = (filename == null || filename.isBlank()) ? "export.xlsx" : filename;
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(safeName, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(XLSX)
                .contentLength(safeBody.length)
                .body(safeBody);
    }
}
