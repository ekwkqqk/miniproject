package com.miniproject.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class MenuRequest {

    private Long parentId;

    @NotBlank(message = "메뉴명을 입력해주세요.")
    @Size(max = 100)
    private String name;

    @Size(max = 200)
    private String url;

    private Integer sortOrder;

    private List<Long> roleIds = new ArrayList<>();

    @Valid
    private List<MenuRoleButtonRequest> roleButtons = new ArrayList<>();

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public List<MenuRoleButtonRequest> getRoleButtons() {
        return roleButtons;
    }

    public void setRoleButtons(List<MenuRoleButtonRequest> roleButtons) {
        this.roleButtons = roleButtons;
    }

    public static class MenuRoleButtonRequest {

        @NotNull
        private Long roleId;

        private boolean canRead = true;
        private boolean canUpdate;
        private boolean canDelete;
        private boolean canUpload;
        private boolean canDownload;
        private boolean canOther;

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public boolean isCanRead() {
            return canRead;
        }

        public void setCanRead(boolean canRead) {
            this.canRead = canRead;
        }

        public boolean isCanUpdate() {
            return canUpdate;
        }

        public void setCanUpdate(boolean canUpdate) {
            this.canUpdate = canUpdate;
        }

        public boolean isCanDelete() {
            return canDelete;
        }

        public void setCanDelete(boolean canDelete) {
            this.canDelete = canDelete;
        }

        public boolean isCanUpload() {
            return canUpload;
        }

        public void setCanUpload(boolean canUpload) {
            this.canUpload = canUpload;
        }

        public boolean isCanDownload() {
            return canDownload;
        }

        public void setCanDownload(boolean canDownload) {
            this.canDownload = canDownload;
        }

        public boolean isCanOther() {
            return canOther;
        }

        public void setCanOther(boolean canOther) {
            this.canOther = canOther;
        }
    }
}
