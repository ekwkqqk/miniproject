package com.miniproject.approval.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApprovalDocumentRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String fileGroupId;

    /** GENERAL / CONFIDENTIAL / TOP_SECRET / URGENT */
    private String docClass;

    /** 예약 상신 시각 (저장만, 실제 예약은 schedule-submit API) */
    private LocalDateTime scheduledSubmitAt;

    @Valid
    private List<ApprovalLineRequest> lines = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFileGroupId() { return fileGroupId; }
    public void setFileGroupId(String fileGroupId) { this.fileGroupId = fileGroupId; }
    public String getDocClass() { return docClass; }
    public void setDocClass(String docClass) { this.docClass = docClass; }
    public LocalDateTime getScheduledSubmitAt() { return scheduledSubmitAt; }
    public void setScheduledSubmitAt(LocalDateTime scheduledSubmitAt) { this.scheduledSubmitAt = scheduledSubmitAt; }
    public List<ApprovalLineRequest> getLines() { return lines; }
    public void setLines(List<ApprovalLineRequest> lines) { this.lines = lines; }

    public static class ApprovalLineRequest {
        @NotNull
        private Integer stepOrder;
        private int sortInStep;
        @NotBlank
        private String lineType;
        @NotNull
        private Long approverId;

        public Integer getStepOrder() { return stepOrder; }
        public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
        public int getSortInStep() { return sortInStep; }
        public void setSortInStep(int sortInStep) { this.sortInStep = sortInStep; }
        public String getLineType() { return lineType; }
        public void setLineType(String lineType) { this.lineType = lineType; }
        public Long getApproverId() { return approverId; }
        public void setApproverId(Long approverId) { this.approverId = approverId; }
    }
}
