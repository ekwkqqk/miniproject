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
        if (roles.isEmpty()) {
            return;
        }

        Menu folder = menuRepository.findAll().stream()
                .filter(m -> "결재".equals(m.getName()) && m.getUrl() == null)
                .findFirst()
                .orElseGet(() -> {
                    Menu created = new Menu("결재", null, 40, null);
                    created.changeNameI18nKey("menu.approval");
                    return menuRepository.save(created);
                });
        folder.changeNameI18nKey("menu.approval");
        menuRepository.save(folder);

        ensureLeaf("상신함", "/approval/submitted", 1, folder, "menu.approvalSubmitted", roles, true);
        ensureLeaf("보류함", "/approval/held", 2, folder, "menu.approvalHeld", roles, true);
        ensureLeaf("미결함", "/approval/pending", 3, folder, "menu.approvalPending", roles, true);
        ensureLeaf("예결함", "/approval/upcoming", 4, folder, "menu.approvalUpcoming", roles, false);
        ensureLeaf("기결함", "/approval/completed", 5, folder, "menu.approvalCompleted", roles, false);
        ensureLeaf("통보함", "/approval/notices", 6, folder, "menu.approvalNotices", roles, false);
        ensureLeaf("기안 작성", "/approval/documents/new", 7, folder, "menu.approvalNew", roles, true);

        removeObsolete("/approval/inbox");
        removeObsolete("/approval/drafts");
        removeObsolete("/approval/documents");
    }

    private void ensureLeaf(String name, String url, int sortOrder, Menu parent, String nameI18nKey,
                            List<Role> roles, boolean canUpdate) {
        Menu menu = menuRepository.findByUrl(url).orElse(null);
        if (menu == null) {
            menu = new Menu(name, url, sortOrder, parent);
            menu.changeNameI18nKey(nameI18nKey);
            menu = menuRepository.save(menu);
        } else {
            menu.changeName(name);
            menu.changeNameI18nKey(nameI18nKey);
            menu.changeSortOrder(sortOrder);
            menu.setParent(parent);
            menuRepository.save(menu);
        }
        ensureRoles(menu, roles, canUpdate);
    }

    private void ensureRoles(Menu menu, List<Role> roles, boolean canUpdate) {
        for (Role role : roles) {
            boolean hasRole = menuRoleRepository.findByMenuId(menu.getId()).stream()
                    .anyMatch(mr -> role.getId().equals(mr.getRoleId()));
            if (!hasRole) {
                menuRoleRepository.save(new MenuRole(menu, role));
            }
            if (menuRoleButtonRepository.findByMenuIdAndRoleId(menu.getId(), role.getId()).isEmpty()) {
                menuRoleButtonRepository.save(new MenuRoleButton(
                        menu, role, true, canUpdate, canUpdate, true, false, true
                ));
            }
        }
    }

    private void removeObsolete(String url) {
        menuRepository.findByUrl(url).ifPresent(menu -> {
            menuRoleButtonRepository.deleteByMenuId(menu.getId());
            menuRoleRepository.deleteByMenuId(menu.getId());
            menuRepository.delete(menu);
        });
    }
}
