package com.miniproject.approval.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ApprovalScheduleRequest {

    @NotNull
    private LocalDateTime scheduledSubmitAt;

    public LocalDateTime getScheduledSubmitAt() { return scheduledSubmitAt; }
    public void setScheduledSubmitAt(LocalDateTime scheduledSubmitAt) { this.scheduledSubmitAt = scheduledSubmitAt; }
}
