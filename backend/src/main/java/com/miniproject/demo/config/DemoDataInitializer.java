package com.miniproject.demo.config;

import com.miniproject.menu.domain.Menu;
import com.miniproject.menu.domain.MenuRepository;
import com.miniproject.menu.domain.MenuRole;
import com.miniproject.menu.domain.MenuRoleButton;
import com.miniproject.menu.domain.MenuRoleButtonRepository;
import com.miniproject.menu.domain.MenuRoleRepository;
import com.miniproject.role.domain.Role;
import com.miniproject.role.domain.RoleRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DemoDataInitializer {

    private final MenuRepository menuRepository;
    private final MenuRoleRepository menuRoleRepository;
    private final MenuRoleButtonRepository menuRoleButtonRepository;
    private final RoleRepository roleRepository;

    public DemoDataInitializer(MenuRepository menuRepository,
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
        if (menuRepository.existsByUrl("/demo/search")) {
            return;
        }

        List<Role> roles = roleRepository.findAll();
        if (roles.isEmpty()) {
            return;
        }

        Menu folder = new Menu("반응형 테스트", null, 90, null);
        folder.changeNameI18nKey("menu.demo");
        folder = menuRepository.save(folder);
        createLeaf("검색 화면", "/demo/search", 1, folder, "menu.demoSearch", roles);
        createLeaf("조회 화면", "/demo/view", 2, folder, "menu.demoView", roles);
        createLeaf("수정 화면", "/demo/edit", 3, folder, "menu.demoEdit", roles);
        createLeaf("팝업 테스트", "/demo/popup", 4, folder, "menu.demoPopup", roles);
    }

    private void createLeaf(String name, String url, int sortOrder, Menu parent, String nameI18nKey, List<Role> roles) {
        Menu menu = new Menu(name, url, sortOrder, parent);
        menu.changeNameI18nKey(nameI18nKey);
        menu = menuRepository.save(menu);
        for (Role role : roles) {
            menuRoleRepository.save(new MenuRole(menu, role));
            menuRoleButtonRepository.save(new MenuRoleButton(
                    menu, role, true, true, false, false, false, false
            ));
        }
    }
}
