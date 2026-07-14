package com.miniproject.mail.domain;

import java.time.LocalDateTime;

public class MailTemplate {

    private Long id;
    private String code;
    private String name;
    private String description;
    /** 발신자 이메일 */
    private String fromAddress;
    /** 발신자 표시 이름 */
    private String fromName;
    /** 기본 수신자 (쉼표 구분) */
    private String toAddresses;
    /** 기본 참조 (쉼표 구분) */
    private String ccAddresses;
    /** 기본 숨은참조 (쉼표 구분) */
    private String bccAddresses;
    private String subject;
    private String body;
    /** true면 HTML 본문 */
    private boolean html;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected MailTemplate() {
    }

    public MailTemplate(String code, String name, String description,
                        String fromAddress, String fromName,
                        String toAddresses, String ccAddresses, String bccAddresses,
                        String subject, String body, boolean html, boolean enabled) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.toAddresses = toAddresses;
        this.ccAddresses = ccAddresses;
        this.bccAddresses = bccAddresses;
        this.subject = subject;
        this.body = body;
        this.html = html;
        this.enabled = enabled;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String name, String description,
                       String fromAddress, String fromName,
                       String toAddresses, String ccAddresses, String bccAddresses,
                       String subject, String body, boolean html, boolean enabled) {
        this.name = name;
        this.description = description;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.toAddresses = toAddresses;
        this.ccAddresses = ccAddresses;
        this.bccAddresses = bccAddresses;
        this.subject = subject;
        this.body = body;
        this.html = html;
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
