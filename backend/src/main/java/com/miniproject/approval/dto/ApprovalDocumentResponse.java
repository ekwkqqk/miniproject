package com.miniproject.approval.dto;

import com.miniproject.approval.domain.ApprovalDocument;
import com.miniproject.approval.domain.ApprovalHistory;
import com.miniproject.approval.domain.ApprovalLine;

import java.time.LocalDateTime;
import java.util.List;

public class ApprovalDocumentResponse {

    private Long id;
    private String docNo;
    private String title;
    private String content;
    private String status;
    private String docClass;
    private Integer currentStep;
    private int version;
    private Long drafterId;
    private String drafterName;
    private String fileGroupId;
    private LocalDateTime scheduledSubmitAt;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ApprovalLineResponse> lines;
    private List<ApprovalHistoryResponse> histories;

    public static ApprovalDocumentResponse from(ApprovalDocument doc, String drafterName,
                                                List<ApprovalLineResponse> lines,
                                                List<ApprovalHistoryResponse> histories) {
        ApprovalDocumentResponse r = new ApprovalDocumentResponse();
        r.id = doc.getId();
        r.docNo = doc.getDocNo();
        r.title = doc.getTitle();
        r.content = doc.getContent();
        r.status = doc.getStatus();
        r.docClass = doc.getDocClass();
        r.currentStep = doc.getCurrentStep();
        r.version = doc.getVersion();
        r.drafterId = doc.getDrafterId();
        r.drafterName = drafterName;
        r.fileGroupId = doc.getFileGroupId();
        r.scheduledSubmitAt = doc.getScheduledSubmitAt();
        r.submittedAt = doc.getSubmittedAt();
        r.completedAt = doc.getCompletedAt();
        r.createdAt = doc.getCreatedAt();
        r.updatedAt = doc.getUpdatedAt();
        r.lines = lines;
        r.histories = histories;
        return r;
    }

    public static ApprovalLineResponse lineFrom(ApprovalLine line) {
        ApprovalLineResponse r = new ApprovalLineResponse();
        r.id = line.getId();
        r.stepOrder = line.getStepOrder();
        r.sortInStep = line.getSortInStep();
        r.lineType = line.getLineType();
        r.approverId = line.getApproverId();
        r.approverName = line.getApproverName();
        r.approverEmail = line.getApproverEmail();
        r.status = line.getStatus();
        r.active = line.isActive();
        r.actedAt = line.getActedAt();
        r.comment = line.getComment();
        return r;
    }

    public static ApprovalHistoryResponse historyFrom(ApprovalHistory h) {
        ApprovalHistoryResponse r = new ApprovalHistoryResponse();
        r.id = h.getId();
        r.actorId = h.getActorId();
        r.actorName = h.getActorName();
        r.action = h.getAction();
        r.stepOrder = h.getStepOrder();
        r.lineId = h.getLineId();
        r.comment = h.getComment();
        r.createdAt = h.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getDocNo() { return docNo; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public String getDocClass() { return docClass; }
    public Integer getCurrentStep() { return currentStep; }
    public int getVersion() { return version; }
    public Long getDrafterId() { return drafterId; }
    public String getDrafterName() { return drafterName; }
    public String getFileGroupId() { return fileGroupId; }
    public LocalDateTime getScheduledSubmitAt() { return scheduledSubmitAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<ApprovalLineResponse> getLines() { return lines; }
    public List<ApprovalHistoryResponse> getHistories() { return histories; }

    public static class ApprovalLineResponse {
        private Long id;
        private int stepOrder;
        private int sortInStep;
        private String lineType;
        private Long approverId;
        private String approverName;
        private String approverEmail;
        private String status;
        private boolean active;
        private LocalDateTime actedAt;
        private String comment;

        public Long getId() { return id; }
        public int getStepOrder() { return stepOrder; }
        public int getSortInStep() { return sortInStep; }
        public String getLineType() { return lineType; }
        public Long getApproverId() { return approverId; }
        public String getApproverName() { return approverName; }
        public String getApproverEmail() { return approverEmail; }
        public String getStatus() { return status; }
        public boolean isActive() { return active; }
        public LocalDateTime getActedAt() { return actedAt; }
        public String getComment() { return comment; }
    }

    public static class ApprovalHistoryResponse {
        private Long id;
        private Long actorId;
        private String actorName;
        private String action;
        private Integer stepOrder;
        private Long lineId;
        private String comment;
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public Long getActorId() { return actorId; }
        public String getActorName() { return actorName; }
        public String getAction() { return action; }
        public Integer getStepOrder() { return stepOrder; }
        public Long getLineId() { return lineId; }
        public String getComment() { return comment; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
