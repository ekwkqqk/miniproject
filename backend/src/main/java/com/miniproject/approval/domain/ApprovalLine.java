package com.miniproject.approval.domain;

import java.time.LocalDateTime;

public class ApprovalLine {

    private Long id;
    private Long documentId;
    private int stepOrder;
    private int sortInStep;
    private String lineType;
    private Long approverId;
    private String status;
    private boolean active;
    private LocalDateTime actedAt;
    private String comment;

    /** join */
    private String approverName;
    private String approverEmail;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public int getStepOrder() { return stepOrder; }
    public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }
    public int getSortInStep() { return sortInStep; }
    public void setSortInStep(int sortInStep) { this.sortInStep = sortInStep; }
    public String getLineType() { return lineType; }
    public void setLineType(String lineType) { this.lineType = lineType; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getActedAt() { return actedAt; }
    public void setActedAt(LocalDateTime actedAt) { this.actedAt = actedAt; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public String getApproverEmail() { return approverEmail; }
    public void setApproverEmail(String approverEmail) { this.approverEmail = approverEmail; }
}
