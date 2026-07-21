package com.miniproject.approval.config;

import com.miniproject.approval.service.ApprovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ApprovalScheduleJob {

    private static final Logger log = LoggerFactory.getLogger(ApprovalScheduleJob.class);

    private final ApprovalService approvalService;

    public ApprovalScheduleJob(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @Scheduled(fixedDelayString = "${app.approval.schedule-poll-ms:60000}")
    public void submitDueDocuments() {
        int count = approvalService.processDueScheduledSubmits();
        if (count > 0) {
            log.info("Reserved approval documents submitted: {}", count);
        }
    }
}
