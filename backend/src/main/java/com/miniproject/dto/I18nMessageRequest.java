package com.miniproject.dto;

import com.miniproject.domain.I18nMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.HashMap;
import java.util.Map;

public class I18nMessageRequest {

    @NotBlank(message = "그룹 코드를 입력해주세요.")
    @Size(max = 100)
    private String groupCode;

    @NotBlank(message = "메시지 코드를 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._-]*$", message = "메시지 코드는 영문으로 시작하는 영문/숫자/._- 만 가능합니다.")
    @Size(max = 150)
    private String code;

    @Size(max = 255)
    private String description;

    /** localeCode -> text. 예: {"ko": "안녕하세요 {name}", "en": "Hello {name}"} */
    private Map<String, String> texts = new HashMap<>();

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getTexts() {
        return texts;
    }

    public void setTexts(Map<String, String> texts) {
        this.texts = texts != null ? texts : new HashMap<>();
    }

    public static record Response(
            Long id,
            Long groupId,
            String groupCode,
            String code,
            String description,
            Map<String, String> texts
    ) {
        public static Response of(I18nMessage message, Map<String, String> texts) {
            return new Response(
                    message.getId(),
                    message.getGroup().getId(),
                    message.getGroup().getCode(),
                    message.getCode(),
                    message.getDescription(),
                    texts
            );
        }
    }
}
