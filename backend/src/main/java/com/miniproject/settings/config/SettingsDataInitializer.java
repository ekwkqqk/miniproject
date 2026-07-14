package com.miniproject.settings.config;

import com.miniproject.menu.domain.Menu;
import com.miniproject.menu.domain.MenuRepository;
import com.miniproject.menu.domain.MenuRole;
import com.miniproject.menu.domain.MenuRoleButton;
import com.miniproject.menu.domain.MenuRoleButtonRepository;
import com.miniproject.menu.domain.MenuRoleRepository;
import com.miniproject.role.domain.Role;
import com.miniproject.role.domain.RoleRepository;
import com.miniproject.role.service.RoleService;
import com.miniproject.settings.service.SystemSettingsService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SettingsDataInitializer {

    private final SystemSettingsService systemSettingsService;
    private final MenuRepository menuRepository;
    private final MenuRoleRepository menuRoleRepository;
    private final MenuRoleButtonRepository menuRoleButtonRepository;
    private final RoleRepository roleRepository;

    public SettingsDataInitializer(SystemSettingsService systemSettingsService,
                                   MenuRepository menuRepository,
                                   MenuRoleRepository menuRoleRepository,
                                   MenuRoleButtonRepository menuRoleButtonRepository,
                                   RoleRepository roleRepository) {
        this.systemSettingsService = systemSettingsService;
        this.menuRepository = menuRepository;
        this.menuRoleRepository = menuRoleRepository;
        this.menuRoleButtonRepository = menuRoleButtonRepository;
        this.roleRepository = roleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        systemSettingsService.getOrCreate();
        ensureSettingsMenu();
    }

    private void ensureSettingsMenu() {
        if (menuRepository.existsByUrl("/admin/settings")) {
            return;
        }
        Role systemAdmin = roleRepository.findByCode(RoleService.SYSTEM_ADMIN_CODE).orElse(null);
        if (systemAdmin == null) {
            return;
        }
        Menu menu = new Menu("시스템 설정", "/admin/settings", 100, null);
        menu.changeNameI18nKey("menu.settings");
        menu = menuRepository.save(menu);
        menuRoleRepository.save(new MenuRole(menu, systemAdmin));
        menuRoleButtonRepository.save(new MenuRoleButton(
                menu, systemAdmin, true, true, false, false, false, false
        ));
    }
}
