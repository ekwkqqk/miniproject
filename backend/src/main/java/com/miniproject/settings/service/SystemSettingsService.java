package com.miniproject.settings.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.role.service.RoleService;
import com.miniproject.settings.domain.SystemSettings;
import com.miniproject.settings.domain.SystemSettingsRepository;
import com.miniproject.settings.dto.SystemSettingsRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemSettingsService {

    private final SystemSettingsRepository systemSettingsRepository;
    private final RoleService roleService;

    public SystemSettingsService(SystemSettingsRepository systemSettingsRepository, RoleService roleService) {
        this.systemSettingsRepository = systemSettingsRepository;
        this.roleService = roleService;
    }

    @Transactional
    public SystemSettings getOrCreate() {
        return systemSettingsRepository.findById(SystemSettings.SINGLETON_ID)
                .orElseGet(() -> systemSettingsRepository.save(SystemSettings.defaults()));
    }

    @Transactional
    public SystemSettingsRequest.Response getSettings() {
        return SystemSettingsRequest.Response.from(getOrCreate());
    }

    @Transactional
    public SystemSettingsRequest.PublicResponse getPublicSettings() {
        return SystemSettingsRequest.PublicResponse.from(getOrCreate());
    }

    @Transactional
    public SystemSettingsRequest.Response update(SystemSettingsRequest request) {
        List<String> roleCodes = request.getDefaultRoleCodes().stream()
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .map(String::toUpperCase)
                .distinct()
                .toList();
        if (roleCodes.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "초기 Role을 하나 이상 선택해주세요.");
        }
        for (String roleCode : roleCodes) {
            roleService.getByCode(roleCode);
        }

        SystemSettings settings = getOrCreate();
        settings.update(
                request.getThemePrimaryColor().trim(),
                request.getPasswordChangePeriodDays(),
                request.getPasswordMinLength(),
                roleCodes,
                request.isAllowMultiLogin()
        );
        systemSettingsRepository.save(settings);
        return SystemSettingsRequest.Response.from(settings);
    }

    @Transactional
    public void validatePasswordLength(String password) {
        SystemSettings settings = getOrCreate();
        if (password == null || password.length() < settings.getPasswordMinLength()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "비밀번호는 " + settings.getPasswordMinLength() + "자 이상이어야 합니다.");
        }
    }
}
