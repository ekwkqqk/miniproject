package com.miniproject.i18n.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

public class I18nResolveRequest {

    @NotBlank(message = "로케일 코드를 입력해주세요.")
    private String locale;

    @NotBlank(message = "그룹 코드를 입력해주세요.")
    private String group;

    @NotBlank(message = "메시지 코드를 입력해주세요.")
    private String code;

    /** 이름 파라미터. 예: {"name": "홍길동", "0": "값"} */
    private Map<String, Object> params = new HashMap<>();

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params != null ? params : new HashMap<>();
    }
}
