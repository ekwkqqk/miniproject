package com.miniproject.mail.dto;

import com.miniproject.mail.domain.MailTemplate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MailTemplateRequest {

    @NotBlank(message = "템플릿 코드를 입력해주세요.")
    @Pattern(regexp = "^[a-z][a-z0-9._-]*$", message = "템플릿 코드는 소문자로 시작하는 영문/숫자/._- 만 가능합니다.")
    @Size(max = 100)
    private String code;

    @NotBlank(message = "템플릿 이름을 입력해주세요.")
    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String description;

    @NotBlank(message = "발신자 이메일을 입력해주세요.")
    @Email(message = "발신자 이메일 형식이 올바르지 않습니다.")
    @Size(max = 255)
    private String fromAddress;

    @Size(max = 150)
    private String fromName;

    @NotBlank(message = "수신자를 입력해주세요.")
    private String toAddresses;

    private String ccAddresses;

    private String bccAddresses;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 500)
    private String subject;

    @NotBlank(message = "본문을 입력해주세요.")
    private String body;

    private Boolean html = true;

    private Boolean enabled = true;

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

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToAddresses() {
        return toAddresses;
    }

    public void setToAddresses(String toAddresses) {
        this.toAddresses = toAddresses;
    }

    public String getCcAddresses() {
        return ccAddresses;
    }

    public void setCcAddresses(String ccAddresses) {
        this.ccAddresses = ccAddresses;
    }

    public String getBccAddresses() {
        return bccAddresses;
    }

    public void setBccAddresses(String bccAddresses) {
        this.bccAddresses = bccAddresses;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Boolean getHtml() {
        return html;
    }

    public void setHtml(Boolean html) {
        this.html = html;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public static record Response(
            Long id,
            String code,
            String name,
            String description,
            String fromAddress,
            String fromName,
            String toAddresses,
            String ccAddresses,
            String bccAddresses,
            String subject,
            String body,
            boolean html,
            boolean enabled
    ) {
        public static Response from(MailTemplate template) {
            return new Response(
                    template.getId(),
                    template.getCode(),
                    template.getName(),
                    template.getDescription(),
                    template.getFromAddress(),
                    template.getFromName(),
                    template.getToAddresses(),
                    template.getCcAddresses(),
                    template.getBccAddresses(),
                    template.getSubject(),
                    template.getBody(),
                    template.isHtml(),
                    template.isEnabled()
            );
        }
    }
}
