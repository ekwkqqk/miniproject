package com.miniproject.approval.domain;

import java.util.Set;

public final class ApprovalStatuses {
    public static final String DRAFT = "DRAFT";
    public static final String SCHEDULED = "SCHEDULED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    public static final String LINE_WAITING = "WAITING";
    public static final String LINE_PENDING = "PENDING";
    public static final String LINE_HELD = "HELD";
    public static final String LINE_APPROVED = "APPROVED";
    public static final String LINE_REJECTED = "REJECTED";
    public static final String LINE_ACKNOWLEDGED = "ACKNOWLEDGED";
    public static final String LINE_SKIPPED = "SKIPPED";

    public static final String TYPE_APPROVE = "APPROVE";
    public static final String TYPE_AGREE = "AGREE";
    public static final String TYPE_POST = "POST";
    public static final String TYPE_NOTIFY = "NOTIFY";

    public static final String CLASS_GENERAL = "GENERAL";
    public static final String CLASS_CONFIDENTIAL = "CONFIDENTIAL";
    public static final String CLASS_TOP_SECRET = "TOP_SECRET";
    public static final String CLASS_URGENT = "URGENT";

    public static final Set<String> DOC_CLASSES = Set.of(
            CLASS_GENERAL, CLASS_CONFIDENTIAL, CLASS_TOP_SECRET, CLASS_URGENT
    );

    private ApprovalStatuses() {}

    public static boolean isBlocking(String lineType) {
        return TYPE_APPROVE.equals(lineType) || TYPE_AGREE.equals(lineType);
    }

    public static String normalizeDocClass(String docClass) {
        if (docClass == null || docClass.isBlank()) {
            return CLASS_GENERAL;
        }
        String value = docClass.trim().toUpperCase();
        if (!DOC_CLASSES.contains(value)) {
            return CLASS_GENERAL;
        }
        return value;
    }
}
