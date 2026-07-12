package com.miniproject.mail.controller;

import com.miniproject.common.ApiResponse;
import com.miniproject.mail.dto.SendMailRequest;
import com.miniproject.mail.service.MailSendService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    private final MailSendService mailSendService;

    public MailController(MailSendService mailSendService) {
        this.mailSendService = mailSendService;
    }

    @PostMapping("/send")
    public ApiResponse<Map<String, Object>> send(@Valid @RequestBody SendMailRequest request) {
        return ApiResponse.success(mailSendService.send(request), "메일 발송 요청이 처리되었습니다.");
    }
}
