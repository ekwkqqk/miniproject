package com.miniproject.mail.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_templates", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mail_templates_code", columnNames = "code")
})
public class MailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String description;

    /** 발신자 이메일 */
    @Column(nullable = false, length = 255)
    private String fromAddress;

    /** 발신자 표시 이름 */
    @Column(length = 150)
    private String fromName;

    /** 기본 수신자 (쉼표 구분) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String toAddresses;

    /** 기본 참조 (쉼표 구분) */
    @Column(columnDefinition = "TEXT")
    private String ccAddresses;

    /** 기본 숨은참조 (쉼표 구분) */
    @Column(columnDefinition = "TEXT")
    private String bccAddresses;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /** true면 HTML 본문 */
    @Column(nullable = false)
    private boolean html;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getFromName() {
        return fromName;
    }

    public String getToAddresses() {
        return toAddresses;
    }

    public String getCcAddresses() {
        return ccAddresses;
    }

    public String getBccAddresses() {
        return bccAddresses;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public boolean isHtml() {
        return html;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
