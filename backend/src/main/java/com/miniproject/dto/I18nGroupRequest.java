package com.miniproject.dto;

import com.miniproject.domain.I18nMessageGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class I18nGroupRequest {

    @NotBlank(message = "그룹 코드를 입력해주세요.")
    @Pattern(regexp = "^[a-z][a-z0-9._-]*$", message = "그룹 코드는 소문자로 시작하는 영문/숫자/._- 만 가능합니다.")
    @Size(max = 100)
    private String code;

    @NotBlank(message = "그룹 이름을 입력해주세요.")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static record Response(Long id, String code, String name, String description) {
        public static Response from(I18nMessageGroup group) {
            return new Response(group.getId(), group.getCode(), group.getName(), group.getDescription());
        }
    }
}
