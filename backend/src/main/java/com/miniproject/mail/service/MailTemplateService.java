package com.miniproject.mail.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.mail.domain.MailTemplate;
import com.miniproject.mail.domain.MailTemplateRepository;
import com.miniproject.mail.dto.MailTemplateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MailTemplateService {

    private final MailTemplateRepository mailTemplateRepository;

    public MailTemplateService(MailTemplateRepository mailTemplateRepository) {
        this.mailTemplateRepository = mailTemplateRepository;
    }

    @Transactional(readOnly = true)
    public List<MailTemplateRequest.Response> getAll() {
        return mailTemplateRepository.findAllByOrderByIdAsc().stream()
                .map(MailTemplateRequest.Response::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MailTemplateRequest.Response getById(Long id) {
        return MailTemplateRequest.Response.from(getRequired(id));
    }

    @Transactional
    public MailTemplateRequest.Response create(MailTemplateRequest request) {
        String code = request.getCode().trim();
        if (mailTemplateRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 존재하는 템플릿 코드입니다.");
        }
        MailTemplate template = mailTemplateRepository.save(new MailTemplate(
                code,
                request.getName().trim(),
                blankToNull(request.getDescription()),
                request.getFromAddress().trim(),
                blankToNull(request.getFromName()),
                request.getToAddresses().trim(),
                blankToNull(request.getCcAddresses()),
                blankToNull(request.getBccAddresses()),
                request.getSubject().trim(),
                request.getBody(),
                request.getHtml() == null || request.getHtml(),
                request.getEnabled() == null || request.getEnabled()
        ));
        return MailTemplateRequest.Response.from(template);
    }

    @Transactional
    public MailTemplateRequest.Response update(Long id, MailTemplateRequest request) {
        MailTemplate template = getRequired(id);
        template.update(
                request.getName().trim(),
                blankToNull(request.getDescription()),
                request.getFromAddress().trim(),
                blankToNull(request.getFromName()),
                request.getToAddresses().trim(),
                blankToNull(request.getCcAddresses()),
                blankToNull(request.getBccAddresses()),
                request.getSubject().trim(),
                request.getBody(),
                request.getHtml() == null || request.getHtml(),
                request.getEnabled() == null || request.getEnabled()
        );
        return MailTemplateRequest.Response.from(template);
    }

    @Transactional
    public void delete(Long id) {
        MailTemplate template = getRequired(id);
        mailTemplateRepository.delete(template);
    }

    @Transactional(readOnly = true)
    public MailTemplate getRequiredByCode(String code) {
        return mailTemplateRepository.findByCode(code.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메일 템플릿을 찾을 수 없습니다."));
    }

    private MailTemplate getRequired(Long id) {
        return mailTemplateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메일 템플릿을 찾을 수 없습니다."));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
