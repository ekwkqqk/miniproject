package com.miniproject.approval.dto;

import jakarta.validation.constraints.NotNull;

public class ApprovalActionRequest {

    @NotNull
    private Long lineId;
    private String comment;

    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
