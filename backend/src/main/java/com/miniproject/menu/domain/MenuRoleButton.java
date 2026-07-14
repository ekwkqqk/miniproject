package com.miniproject.menu.domain;

import com.miniproject.role.domain.Role;

public class MenuRoleButton {

    private Long id;
    private Long menuId;
    private Long roleId;
    private Menu menu;
    private Role role;
    private boolean canRead;
    private boolean canUpdate;
    private boolean canDelete;
    private boolean canUpload;
    private boolean canDownload;
    private boolean canOther;

    protected MenuRoleButton() {
    }

    public MenuRoleButton(Menu menu, Role role,
                          boolean canRead, boolean canUpdate, boolean canDelete,
                          boolean canUpload, boolean canDownload, boolean canOther) {
        this.menu = menu;
        this.role = role;
        this.menuId = menu != null ? menu.getId() : null;
        this.roleId = role != null ? role.getId() : null;
        this.canRead = canRead;
        this.canUpdate = canUpdate;
        this.canDelete = canDelete;
        this.canUpload = canUpload;
        this.canDownload = canDownload;
        this.canOther = canOther;
    }

    public void update(boolean canRead, boolean canUpdate, boolean canDelete,
                       boolean canUpload, boolean canDownload, boolean canOther) {
        this.canRead = canRead;
        this.canUpdate = canUpdate;
        this.canDelete = canDelete;
        this.canUpload = canUpload;
        this.canDownload = canDownload;
        this.canOther = canOther;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
        this.menuId = menu != null ? menu.getId() : this.menuId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        this.roleId = role != null ? role.getId() : this.roleId;
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
