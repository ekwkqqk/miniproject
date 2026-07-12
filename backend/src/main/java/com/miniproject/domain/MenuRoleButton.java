package com.miniproject.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "menu_role_buttons", uniqueConstraints = {
        @UniqueConstraint(name = "uk_menu_role_button", columnNames = {"menu_id", "role_id"})
})
public class MenuRoleButton {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean canRead;

    @Column(nullable = false)
    private boolean canUpdate;

    @Column(nullable = false)
    private boolean canDelete;

    @Column(nullable = false)
    private boolean canUpload;

    @Column(nullable = false)
    private boolean canDownload;

    @Column(nullable = false)
    private boolean canOther;

    protected MenuRoleButton() {
    }

    public MenuRoleButton(Menu menu, Role role,
                          boolean canRead, boolean canUpdate, boolean canDelete,
                          boolean canUpload, boolean canDownload, boolean canOther) {
        this.menu = menu;
        this.role = role;
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

    public Menu getMenu() {
        return menu;
    }

    public Role getRole() {
        return role;
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
