package com.miniproject.i18n.dto;

import com.miniproject.i18n.domain.I18nLocale;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class I18nLocaleRequest {

    @NotBlank(message = "로케일 코드를 입력해주세요.")
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "로케일 코드는 ko, en, ko-KR 형식이어야 합니다.")
    @Size(max = 20)
    private String code;

    @NotBlank(message = "로케일 이름을 입력해주세요.")
    @Size(max = 100)
    private String name;

    private Boolean enabled = true;

    @Min(0)
    @Max(9999)
    private Integer sortOrder = 0;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public static record Response(Long id, String code, String name, boolean enabled, int sortOrder) {
        public static Response from(I18nLocale locale) {
            return new Response(locale.getId(), locale.getCode(), locale.getName(),
                    locale.isEnabled(), locale.getSortOrder());
        }
    }
}
