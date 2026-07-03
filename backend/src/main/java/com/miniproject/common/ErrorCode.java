package com.miniproject.common;

public enum ErrorCode {
    INVALID_INPUT("INVALID_INPUT"),
    UNAUTHORIZED("UNAUTHORIZED"),
    FORBIDDEN("FORBIDDEN"),
    NOT_FOUND("NOT_FOUND"),
    DUPLICATE_EMAIL("DUPLICATE_EMAIL"),
    INTERNAL_ERROR("INTERNAL_ERROR");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
