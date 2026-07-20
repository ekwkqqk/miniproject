package com.miniproject.approval.domain;

public final class ApprovalStatuses {
    public static final String DRAFT = "DRAFT";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    public static final String LINE_WAITING = "WAITING";
    public static final String LINE_PENDING = "PENDING";
    public static final String LINE_APPROVED = "APPROVED";
    public static final String LINE_REJECTED = "REJECTED";
    public static final String LINE_ACKNOWLEDGED = "ACKNOWLEDGED";
    public static final String LINE_SKIPPED = "SKIPPED";

    public static final String TYPE_APPROVE = "APPROVE";
    public static final String TYPE_AGREE = "AGREE";
    public static final String TYPE_POST = "POST";
    public static final String TYPE_NOTIFY = "NOTIFY";

    private ApprovalStatuses() {}

    public static boolean isBlocking(String lineType) {
        return TYPE_APPROVE.equals(lineType) || TYPE_AGREE.equals(lineType);
    }
}
