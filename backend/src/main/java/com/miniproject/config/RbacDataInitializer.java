package com.miniproject.config;

import com.miniproject.menu.domain.Menu;
import com.miniproject.menu.domain.MenuRepository;
import com.miniproject.menu.domain.MenuRole;
import com.miniproject.menu.domain.MenuRoleButton;
import com.miniproject.menu.domain.MenuRoleButtonRepository;
import com.miniproject.menu.domain.MenuRoleRepository;
import com.miniproject.role.domain.Role;
import com.miniproject.role.domain.RoleRepository;
import com.miniproject.role.domain.UserRole;
import com.miniproject.role.domain.UserRoleRepository;
import com.miniproject.role.service.RoleService;
import com.miniproject.user.domain.User;
import com.miniproject.user.domain.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class RbacDataInitializer {

    public static final String SYSTEM_ADMIN_EMAIL = "admin@system.local";

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final MenuRoleRepository menuRoleRepository;
    private final MenuRoleButtonRepository menuRoleButtonRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String systemAdminPassword;

    public RbacDataInitializer(RoleRepository roleRepository,
                               MenuRepository menuRepository,
                               MenuRoleRepository menuRoleRepository,
                               MenuRoleButtonRepository menuRoleButtonRepository,
                               UserRepository userRepository,
                               UserRoleRepository userRoleRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${app.seed.system-admin-password:Admin123!}") String systemAdminPassword) {
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.menuRoleRepository = menuRoleRepository;
        this.menuRoleButtonRepository = menuRoleButtonRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.systemAdminPassword = systemAdminPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        Role systemAdmin = ensureRole(RoleService.SYSTEM_ADMIN_CODE, "시스템관리자", "시스템 전체 관리");
        Role specialUser = ensureRole("SPECIAL_USER", "특별사용자", "특별 기능 접근");
        Role userRole = ensureRole(RoleService.USER_CODE, "일반사용자", "기본 사용자");

        ensureSystemAdminUser(systemAdmin);
        ensureDefaultMenus(systemAdmin, specialUser, userRole);
    }

    private Role ensureRole(String code, String name, String description) {
        return roleRepository.findByCode(code)
                .orElseGet(() -> roleRepository.save(new Role(code, name, description)));
    }

    private void ensureSystemAdminUser(Role systemAdmin) {
        User admin = userRepository.findByEmail(SYSTEM_ADMIN_EMAIL).orElseGet(() ->
                userRepository.save(new User(
                        SYSTEM_ADMIN_EMAIL,
                        passwordEncoder.encode(systemAdminPassword),
                        "시스템관리자"
                ))
        );

        if (!userRoleRepository.existsByUser_EmailAndRole_Code(admin.getEmail(), RoleService.SYSTEM_ADMIN_CODE)) {
            userRoleRepository.save(new UserRole(admin, systemAdmin));
        }
    }

    private void ensureDefaultMenus(Role systemAdmin, Role specialUser, Role userRole) {
        if (!menuRepository.findAll().isEmpty()) {
            return;
        }

        createMenu("대시보드", "/", 1, null, "menu.dashboard",
                List.of(systemAdmin, specialUser, userRole), true, false, false, false, false, false);
        createMenu("특별 사용자", "/special", 2, null, "menu.special",
                List.of(systemAdmin, specialUser), true, true, false, false, false, false);

        Menu adminFolder = createMenu("시스템 관리", null, 3, null, "menu.system",
                List.of(), false, false, false, false, false, false);
        createMenu("사용자 Role 관리", "/admin/users", 1, adminFolder, "menu.users",
                List.of(systemAdmin), true, true, false, false, false, false);
        createMenu("Role 관리", "/admin/roles", 2, adminFolder, "menu.roles",
                List.of(systemAdmin), true, true, true, false, false, false);
        createMenu("메뉴 관리", "/admin/menus", 3, adminFolder, "menu.menus",
                List.of(systemAdmin), true, true, true, false, false, false);
    }

    private Menu createMenu(String name, String url, int sortOrder, Menu parent, String nameI18nKey,
                            List<Role> roles,
                            boolean canRead, boolean canUpdate, boolean canDelete,
                            boolean canUpload, boolean canDownload, boolean canOther) {
        Menu menu = new Menu(name, url, sortOrder, parent);
        if (nameI18nKey != null) {
            menu.changeNameI18nKey(nameI18nKey);
        }
        menu = menuRepository.save(menu);
        for (Role role : roles) {
            menuRoleRepository.save(new MenuRole(menu, role));
            menuRoleButtonRepository.save(new MenuRoleButton(
                    menu, role, canRead, canUpdate, canDelete, canUpload, canDownload, canOther
            ));
        }
        return menu;
    }
}
