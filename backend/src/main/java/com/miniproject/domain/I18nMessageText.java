package com.miniproject.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "i18n_message_texts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_i18n_message_texts", columnNames = {"message_id", "locale_id"})
})
public class I18nMessageText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private I18nMessage message;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "locale_id", nullable = false)
    private I18nLocale locale;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    protected I18nMessageText() {
    }

    public I18nMessageText(I18nMessage message, I18nLocale locale, String text) {
        this.message = message;
        this.locale = locale;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public I18nMessage getMessage() {
        return message;
    }

    public I18nLocale getLocale() {
        return locale;
    }

    public String getText() {
        return text;
    }

    public void changeText(String text) {
        this.text = text;
    }
}
