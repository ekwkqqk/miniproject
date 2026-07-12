package com.miniproject.menu.dto;

import com.miniproject.role.dto.RoleResponse;

import java.util.ArrayList;
import java.util.List;

public class MenuResponse {

    private final Long id;
    private final Long parentId;
    private final String name;
    private final String nameI18nKey;
    private final String url;
    private final int sortOrder;
    private final boolean folder;
    private final List<RoleResponse> roles;
    private final List<MenuRoleButtonResponse> roleButtons;
    private final ButtonPermissionDto buttons;
    private final List<MenuResponse> children;

    public MenuResponse(Long id, Long parentId, String name, String nameI18nKey, String url, int sortOrder,
                        boolean folder,
                        List<RoleResponse> roles,
                        List<MenuRoleButtonResponse> roleButtons,
                        ButtonPermissionDto buttons,
                        List<MenuResponse> children) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.nameI18nKey = nameI18nKey;
        this.url = url;
        this.sortOrder = sortOrder;
        this.folder = folder;
        this.roles = roles;
        this.roleButtons = roleButtons;
        this.buttons = buttons;
        this.children = children != null ? children : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getNameI18nKey() {
        return nameI18nKey;
    }

    public String getUrl() {
        return url;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isFolder() {
        return folder;
    }

    public List<RoleResponse> getRoles() {
        return roles;
    }

    public List<MenuRoleButtonResponse> getRoleButtons() {
        return roleButtons;
    }

    public ButtonPermissionDto getButtons() {
        return buttons;
    }

    public List<MenuResponse> getChildren() {
        return children;
    }

    public static class MenuRoleButtonResponse {
        private final Long roleId;
        private final String roleCode;
        private final String roleName;
        private final ButtonPermissionDto buttons;

        public MenuRoleButtonResponse(Long roleId, String roleCode, String roleName, ButtonPermissionDto buttons) {
            this.roleId = roleId;
            this.roleCode = roleCode;
            this.roleName = roleName;
            this.buttons = buttons;
        }

        public Long getRoleId() {
            return roleId;
        }

        public String getRoleCode() {
            return roleCode;
        }

        public String getRoleName() {
            return roleName;
        }

        public ButtonPermissionDto getButtons() {
            return buttons;
        }
    }
}
