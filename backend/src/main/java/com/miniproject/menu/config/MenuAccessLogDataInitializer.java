package com.miniproject.menu.config;

import com.miniproject.menu.domain.Menu;
import com.miniproject.menu.domain.MenuRepository;
import com.miniproject.menu.domain.MenuRole;
import com.miniproject.menu.domain.MenuRoleButton;
import com.miniproject.menu.domain.MenuRoleButtonRepository;
import com.miniproject.menu.domain.MenuRoleRepository;
import com.miniproject.role.domain.Role;
import com.miniproject.role.domain.RoleRepository;
import com.miniproject.role.service.RoleService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MenuAccessLogDataInitializer {

    private final MenuRepository menuRepository;
    private final MenuRoleRepository menuRoleRepository;
    private final MenuRoleButtonRepository menuRoleButtonRepository;
    private final RoleRepository roleRepository;

    public MenuAccessLogDataInitializer(MenuRepository menuRepository,
                                        MenuRoleRepository menuRoleRepository,
                                        MenuRoleButtonRepository menuRoleButtonRepository,
                                        RoleRepository roleRepository) {
        this.menuRepository = menuRepository;
        this.menuRoleRepository = menuRoleRepository;
        this.menuRoleButtonRepository = menuRoleButtonRepository;
        this.roleRepository = roleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (menuRepository.existsByUrl("/admin/menu-access-logs")) {
            return;
        }
        Role systemAdmin = roleRepository.findByCode(RoleService.SYSTEM_ADMIN_CODE).orElse(null);
        if (systemAdmin == null) {
            return;
        }
        Menu menu = menuRepository.save(new Menu("메뉴 접근 이력", "/admin/menu-access-logs", 110, null));
        menuRoleRepository.save(new MenuRole(menu, systemAdmin));
        menuRoleButtonRepository.save(new MenuRoleButton(
                menu, systemAdmin, true, false, false, false, false, false
        ));
    }
}
