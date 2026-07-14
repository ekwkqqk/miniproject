package com.miniproject.settings.dto;

import com.miniproject.settings.domain.SystemSettings;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SystemSettingsRequest {

    @NotBlank(message = "테마 색상을 입력해주세요.")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "테마 색상은 #RGB 또는 #RRGGBB 형식이어야 합니다.")
    private String themePrimaryColor;

    @Min(value = 0, message = "비밀번호 주기는 0 이상이어야 합니다.")
    @Max(value = 3650, message = "비밀번호 주기는 3650일 이하여야 합니다.")
    private int passwordChangePeriodDays;

    @Min(value = 4, message = "비밀번호 최소 길이는 4 이상이어야 합니다.")
    @Max(value = 128, message = "비밀번호 최소 길이는 128 이하여야 합니다.")
    private int passwordMinLength;

    @NotEmpty(message = "초기 Role을 하나 이상 선택해주세요.")
    private List<@NotBlank @Size(max = 50) String> defaultRoleCodes;

    private boolean allowMultiLogin;

    @Min(value = 0, message = "로그인 실패 잠금 횟수는 0 이상이어야 합니다.")
    @Max(value = 100, message = "로그인 실패 잠금 횟수는 100 이하여야 합니다.")
    private int maxFailedLoginAttempts;

    public String getThemePrimaryColor() {
        return themePrimaryColor;
    }

    public void setThemePrimaryColor(String themePrimaryColor) {
        this.themePrimaryColor = themePrimaryColor;
    }

    public int getPasswordChangePeriodDays() {
        return passwordChangePeriodDays;
    }

    public void setPasswordChangePeriodDays(int passwordChangePeriodDays) {
        this.passwordChangePeriodDays = passwordChangePeriodDays;
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public List<String> getDefaultRoleCodes() {
        return defaultRoleCodes;
    }

    public void setDefaultRoleCodes(List<String> defaultRoleCodes) {
        this.defaultRoleCodes = defaultRoleCodes;
    }

    public boolean isAllowMultiLogin() {
        return allowMultiLogin;
    }

    public void setAllowMultiLogin(boolean allowMultiLogin) {
        this.allowMultiLogin = allowMultiLogin;
    }

    public int getMaxFailedLoginAttempts() {
        return maxFailedLoginAttempts;
    }

    public void setMaxFailedLoginAttempts(int maxFailedLoginAttempts) {
        this.maxFailedLoginAttempts = maxFailedLoginAttempts;
    }

    public static record Response(
            String themePrimaryColor,
            int passwordChangePeriodDays,
            int passwordMinLength,
            List<String> defaultRoleCodes,
            boolean allowMultiLogin,
            int maxFailedLoginAttempts
    ) {
        public static Response from(SystemSettings settings) {
            return new Response(
                    settings.getThemePrimaryColor(),
                    settings.getPasswordChangePeriodDays(),
                    settings.getPasswordMinLength(),
                    settings.getDefaultRoleCodes(),
                    settings.isAllowMultiLogin(),
                    settings.getMaxFailedLoginAttempts()
            );
        }
    }

    public static record PublicResponse(
            String themePrimaryColor,
            int passwordMinLength
    ) {
        public static PublicResponse from(SystemSettings settings) {
            return new PublicResponse(
                    settings.getThemePrimaryColor(),
                    settings.getPasswordMinLength()
            );
        }
    }
}
