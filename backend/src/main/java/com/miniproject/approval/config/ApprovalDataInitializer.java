package com.miniproject.approval.config;

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
public class ApprovalDataInitializer {

    private final MenuRepository menuRepository;
    private final MenuRoleRepository menuRoleRepository;
    private final MenuRoleButtonRepository menuRoleButtonRepository;
    private final RoleRepository roleRepository;

    public ApprovalDataInitializer(MenuRepository menuRepository,
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
        if (roles.isEmpty() || menuRepository.existsByUrl("/approval/inbox")) {
            return;
        }
        Menu folder = new Menu("결재", null, 40, null);
        folder.changeNameI18nKey("menu.approval");
        folder = menuRepository.save(folder);
        createLeaf("대기함", "/approval/inbox", 1, folder, "menu.approvalInbox", roles, true);
        createLeaf("통보함", "/approval/notices", 2, folder, "menu.approvalNotices", roles, false);
        createLeaf("기안함", "/approval/drafts", 3, folder, "menu.approvalDrafts", roles, true);
        createLeaf("문서함", "/approval/documents", 4, folder, "menu.approvalDocuments", roles, false);
        createLeaf("기안 작성", "/approval/documents/new", 5, folder, "menu.approvalNew", roles, true);
    }

    private void createLeaf(String name, String url, int sortOrder, Menu parent, String nameI18nKey,
                            List<Role> roles, boolean canUpdate) {
        Menu menu = new Menu(name, url, sortOrder, parent);
        menu.changeNameI18nKey(nameI18nKey);
        menu = menuRepository.save(menu);
        for (Role role : roles) {
            menuRoleRepository.save(new MenuRole(menu, role));
            menuRoleButtonRepository.save(new MenuRoleButton(
                    menu, role, true, canUpdate, canUpdate, true, false, true
            ));
        }
    }
}
