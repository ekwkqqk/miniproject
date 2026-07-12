package com.miniproject.dto;

import java.util.List;

public class ButtonPermissionDto {

    private final boolean canRead;
    private final boolean canUpdate;
    private final boolean canDelete;
    private final boolean canUpload;
    private final boolean canDownload;
    private final boolean canOther;

    public ButtonPermissionDto(boolean canRead, boolean canUpdate, boolean canDelete,
                               boolean canUpload, boolean canDownload, boolean canOther) {
        this.canRead = canRead;
        this.canUpdate = canUpdate;
        this.canDelete = canDelete;
        this.canUpload = canUpload;
        this.canDownload = canDownload;
        this.canOther = canOther;
    }

    public static ButtonPermissionDto none() {
        return new ButtonPermissionDto(false, false, false, false, false, false);
    }

    public static ButtonPermissionDto merge(List<ButtonPermissionDto> list) {
        boolean canRead = false;
        boolean canUpdate = false;
        boolean canDelete = false;
        boolean canUpload = false;
        boolean canDownload = false;
        boolean canOther = false;
        for (ButtonPermissionDto item : list) {
            canRead |= item.canRead;
            canUpdate |= item.canUpdate;
            canDelete |= item.canDelete;
            canUpload |= item.canUpload;
            canDownload |= item.canDownload;
            canOther |= item.canOther;
        }
        return new ButtonPermissionDto(canRead, canUpdate, canDelete, canUpload, canDownload, canOther);
    }

    public boolean isCanRead() {
        return canRead;
    }

    public boolean isCanUpdate() {
        return canUpdate;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public boolean isCanUpload() {
        return canUpload;
    }

    public boolean isCanDownload() {
        return canDownload;
    }

    public boolean isCanOther() {
        return canOther;
    }
}
