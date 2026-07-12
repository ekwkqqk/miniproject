package com.miniproject.mail.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.mail.config.MailAppProperties;
import com.miniproject.mail.domain.MailTemplate;
import com.miniproject.mail.dto.SendMailRequest;
import com.miniproject.mail.util.MailTemplateRenderer;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MailSendService {

    private static final Logger log = LoggerFactory.getLogger(MailSendService.class);

    private final MailTemplateService mailTemplateService;
    private final MailAppProperties mailAppProperties;
    private final ObjectProvider<JavaMailSender> javaMailSender;

    public MailSendService(MailTemplateService mailTemplateService,
                           MailAppProperties mailAppProperties,
                           ObjectProvider<JavaMailSender> javaMailSender) {
        this.mailTemplateService = mailTemplateService;
        this.mailAppProperties = mailAppProperties;
        this.javaMailSender = javaMailSender;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> send(SendMailRequest request) {
        MailTemplate template = mailTemplateService.getRequiredByCode(request.getTemplateCode());
        if (!template.isEnabled()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "비활성화된 메일 템플릿입니다.");
        }

        Map<String, Object> params = request.getParams() != null ? request.getParams() : Map.of();
        String subject = MailTemplateRenderer.render(template.getSubject(), params);
        String body = MailTemplateRenderer.render(template.getBody(), params);

        List<String> to = resolveAddresses(request.getTo(), template.getToAddresses());
        List<String> cc = resolveAddresses(request.getCc(), template.getCcAddresses());
        List<String> bcc = resolveAddresses(request.getBcc(), template.getBccAddresses());

        if (to.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "수신자가 없습니다.");
        }

        if (!mailAppProperties.isEnabled()) {
            log.info("""
                    [MAIL DRY-RUN] template={}, from={} <{}>, to={}, cc={}, bcc={}, subject={}, html={}, body={}
                    """,
                    template.getCode(),
                    template.getFromName(),
                    template.getFromAddress(),
                    to, cc, bcc, subject, template.isHtml(), body);
        } else {
            sendViaSmtp(template, subject, body, to, cc, bcc);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("templateCode", template.getCode());
        result.put("fromAddress", template.getFromAddress());
        result.put("fromName", template.getFromName());
        result.put("to", to);
        result.put("cc", cc);
        result.put("bcc", bcc);
        result.put("subject", subject);
        result.put("dryRun", !mailAppProperties.isEnabled());
        return result;
    }

    private void sendViaSmtp(MailTemplate template, String subject, String body,
                             List<String> to, List<String> cc, List<String> bcc) {
        JavaMailSender sender = javaMailSender.getIfAvailable();
        if (sender == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "SMTP가 설정되지 않았습니다. spring.mail.host 등을 확인해주세요.");
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            if (template.getFromName() != null && !template.getFromName().isBlank()) {
                helper.setFrom(new InternetAddress(template.getFromAddress(), template.getFromName()));
            } else {
                helper.setFrom(template.getFromAddress());
            }
            helper.setTo(to.toArray(String[]::new));
            if (!cc.isEmpty()) {
                helper.setCc(cc.toArray(String[]::new));
            }
            if (!bcc.isEmpty()) {
                helper.setBcc(bcc.toArray(String[]::new));
            }
            helper.setSubject(subject);
            helper.setText(body, template.isHtml());
            sender.send(message);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("메일 발송 실패: {}", ex.getMessage(), ex);
            throw new BusinessException(ErrorCode.INVALID_INPUT, "메일 발송에 실패했습니다: " + ex.getMessage());
        }
    }

    private List<String> resolveAddresses(List<String> override, String templateDefault) {
        if (override != null && !override.isEmpty()) {
            return override.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        return MailTemplateRenderer.parseAddresses(templateDefault);
    }
}
