package com.miniproject.menu.domain;

import com.miniproject.role.domain.Role;

public class MenuRole {

    private Long id;
    private Long menuId;
    private Long roleId;
    private Menu menu;
    private Role role;

    protected MenuRole() {
    }

    public MenuRole(Menu menu, Role role) {
        this.menu = menu;
        this.role = role;
        this.menuId = menu != null ? menu.getId() : null;
        this.roleId = role != null ? role.getId() : null;
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
}
