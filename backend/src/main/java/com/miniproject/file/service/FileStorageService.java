package com.miniproject.file.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.file.config.FileAppProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootDir;

    public FileStorageService(FileAppProperties properties) {
        this.rootDir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = extensionOf(original);
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = rootDir.resolve(storedName).normalize();
        if (!target.startsWith(rootDir)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "잘못된 파일 경로입니다.");
        }
        try {
            Files.createDirectories(rootDir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return storedName;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "파일 저장에 실패했습니다.");
        }
    }

    public Resource loadAsResource(String storedName) {
        try {
            Path file = rootDir.resolve(storedName).normalize();
            if (!file.startsWith(rootDir) || !Files.exists(file)) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "파일을 찾을 수 없습니다.");
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "파일을 읽을 수 없습니다.");
            }
            return resource;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "파일 로드에 실패했습니다.");
        }
    }

    public void deleteQuietly(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        try {
            Path file = rootDir.resolve(storedName).normalize();
            if (file.startsWith(rootDir)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException ignored) {
            // best-effort
        }
    }

    private static String extensionOf(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) {
            return "";
        }
        String ext = originalName.substring(dot);
        if (ext.length() > 32 || ext.contains("/") || ext.contains("\\")) {
            return "";
        }
        return ext;
    }
}
