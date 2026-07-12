package com.miniproject.mail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class MailAppProperties {

    /** false면 실제 SMTP 대신 로그로만 출력 (로컬 기본) */
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
