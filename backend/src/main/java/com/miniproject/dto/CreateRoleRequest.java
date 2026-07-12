package com.miniproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateRoleRequest {

    @NotBlank(message = "Role 코드를 입력해주세요.")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "Role 코드는 대문자/숫자/언더스코어만 가능합니다.")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Role 이름을 입력해주세요.")
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
}
