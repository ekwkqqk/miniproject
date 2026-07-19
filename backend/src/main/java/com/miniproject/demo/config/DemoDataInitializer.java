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
        List<Role> roles = roleRepository.findAll();
        if (roles.isEmpty()) {
            return;
        }

        if (!menuRepository.existsByUrl("/demo/search")) {
            Menu folder = new Menu("반응형 테스트", null, 90, null);
            folder.changeNameI18nKey("menu.demo");
            folder = menuRepository.save(folder);
            createLeaf("검색 화면", "/demo/search", 1, folder, "menu.demoSearch", roles, false, true);
            createLeaf("조회 화면", "/demo/view", 2, folder, "menu.demoView", roles, false, false);
            createLeaf("수정 화면", "/demo/edit", 3, folder, "menu.demoEdit", roles, false, false);
            createLeaf("팝업 테스트", "/demo/popup", 4, folder, "menu.demoPopup", roles, false, false);
            createLeaf("파일 첨부", "/demo/upload", 5, folder, "menu.demoUpload", roles, true, true);
            return;
        }

        ensureUploadMenu(roles);
        ensureSearchDownload(roles);
    }

    private void ensureUploadMenu(List<Role> roles) {
        if (menuRepository.existsByUrl("/demo/upload")) {
            return;
        }
        Menu parent = menuRepository.findByUrl("/demo/search")
                .map(Menu::getParentId)
                .flatMap(id -> id == null ? java.util.Optional.empty() : menuRepository.findById(id))
                .orElse(null);
        createLeaf("파일 첨부", "/demo/upload", 5, parent, "menu.demoUpload", roles, true, true);
    }

    /** 기존 DB에도 검색 화면 엑셀 다운로드 권한을 부여한다. */
    private void ensureSearchDownload(List<Role> roles) {
        menuRepository.findByUrl("/demo/search").ifPresent(menu -> {
            for (Role role : roles) {
                menuRoleButtonRepository.findByMenuIdAndRoleId(menu.getId(), role.getId())
                        .ifPresent(button -> {
                            if (!button.isCanDownload()) {
                                button.update(
                                        button.isCanRead(),
                                        button.isCanUpdate(),
                                        button.isCanDelete(),
                                        button.isCanUpload(),
                                        true,
                                        button.isCanOther()
                                );
                                menuRoleButtonRepository.save(button);
                            }
                        });
            }
        });
    }

    private void createLeaf(String name, String url, int sortOrder, Menu parent, String nameI18nKey,
                            List<Role> roles, boolean upload, boolean download) {
        Menu menu = new Menu(name, url, sortOrder, parent);
        menu.changeNameI18nKey(nameI18nKey);
        menu = menuRepository.save(menu);
        for (Role role : roles) {
            menuRoleRepository.save(new MenuRole(menu, role));
            menuRoleButtonRepository.save(new MenuRoleButton(
                    menu, role, true, true, true, upload, download, false
            ));
        }
    }
}
