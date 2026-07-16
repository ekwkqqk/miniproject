package com.miniproject.file.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.file.domain.StoredFile;
import com.miniproject.file.domain.StoredFileRepository;
import com.miniproject.file.dto.FileResponse;
import com.miniproject.file.dto.FileUploadResult;
import com.miniproject.user.domain.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileService {

    private final StoredFileRepository storedFileRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public FileService(StoredFileRepository storedFileRepository,
                       FileStorageService fileStorageService,
                       UserRepository userRepository) {
        this.storedFileRepository = storedFileRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @Transactional
    public FileUploadResult upload(MultipartFile[] files,
                                   String accept,
                                   Integer limit,
                                   String uploaderEmail) {
        if (files == null || files.length == 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "업로드할 파일이 없습니다.");
        }

        List<MultipartFile> nonEmpty = Arrays.stream(files)
                .filter(f -> f != null && !f.isEmpty())
                .toList();
        if (nonEmpty.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "업로드할 파일이 없습니다.");
        }

        if (limit != null && limit > 0 && nonEmpty.size() > limit) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "파일은 최대 " + limit + "개까지 업로드할 수 있습니다.");
        }

        Set<String> acceptRules = parseAccept(accept);
        Long uploaderId = userRepository.findByEmail(uploaderEmail)
                .map(u -> u.getId())
                .orElse(null);

        Long fileGroupId = storedFileRepository.nextFileGroupId();
        List<FileResponse> results = new ArrayList<>();
        for (MultipartFile file : nonEmpty) {
            validateAccept(file, acceptRules);
            String storedName = fileStorageService.store(file);
            String originalName = StringUtils.hasText(file.getOriginalFilename())
                    ? file.getOriginalFilename()
                    : storedName;
            StoredFile entity = new StoredFile(
                    fileGroupId,
                    originalName,
                    storedName,
                    file.getContentType(),
                    file.getSize(),
                    uploaderId,
                    uploaderEmail
            );
            storedFileRepository.save(entity);
            results.add(FileResponse.from(entity));
        }

        List<Long> fileIds = results.stream().map(FileResponse::id).toList();
        return new FileUploadResult(fileGroupId, fileIds, results);
    }

    @Transactional(readOnly = true)
    public FileResponse get(Long id) {
        return FileResponse.from(requireFile(id));
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getByFileGroupId(Long fileGroupId) {
        return storedFileRepository.findByFileGroupId(fileGroupId).stream()
                .map(FileResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StoredFile requireFile(Long id) {
        return storedFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "파일을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Resource loadResource(Long id) {
        return fileStorageService.loadAsResource(requireFile(id).getStoredName());
    }

    @Transactional
    public void delete(Long id) {
        StoredFile file = requireFile(id);
        storedFileRepository.delete(file);
        fileStorageService.deleteQuietly(file.getStoredName());
    }

    private static Set<String> parseAccept(String accept) {
        if (!StringUtils.hasText(accept)) {
            return Set.of();
        }
        return Arrays.stream(accept.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private static void validateAccept(MultipartFile file, Set<String> rules) {
        if (rules.isEmpty()) {
            return;
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);

        for (String rule : rules) {
            if (rule.startsWith(".")) {
                if (name.endsWith(rule)) {
                    return;
                }
            } else if (rule.endsWith("/*")) {
                String prefix = rule.substring(0, rule.length() - 1);
                if (contentType.startsWith(prefix)) {
                    return;
                }
            } else if (rule.equals(contentType) || (name.endsWith("." + rule) && !rule.contains("/"))) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.INVALID_INPUT,
                "허용되지 않은 파일 형식입니다: " + (StringUtils.hasText(name) ? name : contentType));
    }
}
