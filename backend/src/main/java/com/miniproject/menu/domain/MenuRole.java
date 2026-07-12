package com.miniproject.menu.domain;

import com.miniproject.role.domain.Role;

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
@Table(name = "menu_roles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_menu_role", columnNames = {"menu_id", "role_id"})
})
public class MenuRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    protected MenuRole() {
    }

    public MenuRole(Menu menu, Role role) {
        this.menu = menu;
        this.role = role;
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
}
