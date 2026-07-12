package com.miniproject.mail.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendMailRequest {

    @NotBlank(message = "템플릿 코드를 입력해주세요.")
    private String templateCode;

    /** 미입력 시 템플릿 기본 수신자 사용 */
    private List<String> to;

    private List<String> cc;

    private List<String> bcc;

    /** 제목/본문 {name} 등 파라미터 */
    private Map<String, Object> params = new HashMap<>();

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params != null ? params : new HashMap<>();
    }
}
