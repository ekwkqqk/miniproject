package com.miniproject.i18n.domain;

public class I18nMessageText {

    private Long id;
    private Long messageId;
    private Long localeId;
    private I18nMessage message;
    private I18nLocale locale;
    private String text;

    protected I18nMessageText() {
    }

    public I18nMessageText(I18nMessage message, I18nLocale locale, String text) {
        this.message = message;
        this.locale = locale;
        this.messageId = message != null ? message.getId() : null;
        this.localeId = locale != null ? locale.getId() : null;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getLocaleId() {
        return localeId;
    }

    public void setLocaleId(Long localeId) {
        this.localeId = localeId;
    }

    public I18nMessage getMessage() {
        return message;
    }

    public void setMessage(I18nMessage message) {
        this.message = message;
        this.messageId = message != null ? message.getId() : this.messageId;
    }

    public I18nLocale getLocale() {
        return locale;
    }

    public void setLocale(I18nLocale locale) {
        this.locale = locale;
        this.localeId = locale != null ? locale.getId() : this.localeId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void changeText(String text) {
        this.text = text;
    }
}
