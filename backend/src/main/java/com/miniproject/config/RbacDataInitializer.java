package com.miniproject.config;

import com.miniproject.menu.domain.Menu;
import com.miniproject.menu.domain.MenuRepository;
import com.miniproject.menu.domain.MenuRole;
import com.miniproject.menu.domain.MenuRoleButton;
import com.miniproject.menu.domain.MenuRoleButtonRepository;
import com.miniproject.menu.domain.MenuRoleRepository;
import com.miniproject.role.domain.Role;
import com.miniproject.role.domain.RoleRepository;
import com.miniproject.user.domain.User;
import com.miniproject.user.domain.UserRepository;
import com.miniproject.role.domain.UserRole;
import com.miniproject.role.domain.UserRoleRepository;
import com.miniproject.role.service.RoleService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class RbacDataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final MenuRepository menuRepository;
    private final MenuRoleRepository menuRoleRepository;
    private final MenuRoleButtonRepository menuRoleButtonRepository;
    private final JdbcTemplate jdbcTemplate;

    public RbacDataInitializer(RoleRepository roleRepository,
                               UserRepository userRepository,
                               UserRoleRepository userRoleRepository,
                               MenuRepository menuRepository,
                               MenuRoleRepository menuRoleRepository,
                               MenuRoleButtonRepository menuRoleButtonRepository,
                               JdbcTemplate jdbcTemplate) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.menuRepository = menuRepository;
        this.menuRoleRepository = menuRoleRepository;
        this.menuRoleButtonRepository = menuRoleButtonRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        migrateSchema();
        Role systemAdmin = ensureRole(RoleService.SYSTEM_ADMIN_CODE, "시스템관리자", "시스템 전체 관리");
        Role specialUser = ensureRole("SPECIAL_USER", "특별사용자", "특별 기능 접근");
        Role userRole = ensureRole(RoleService.USER_CODE, "일반사용자", "기본 사용자");

        migrateLegacyUserRoles(systemAdmin, specialUser, userRole);
        ensureDefaultMenus(systemAdmin, specialUser, userRole);
    }

    private Role ensureRole(String code, String name, String description) {
        return roleRepository.findByCode(code)
                .orElseGet(() -> roleRepository.save(new Role(code, name, description)));
    }

    private void migrateLegacyUserRoles(Role systemAdmin, Role specialUser, Role userRole) {
        for (User user : userRepository.findAll()) {
            if (!userRoleRepository.findByUserId(user.getId()).isEmpty()) {
                continue;
            }
            String legacyRole = findLegacyRole(user.getId());
            Role mapped = switch (legacyRole == null ? "USER" : legacyRole) {
                case "SYSTEM_ADMIN", "ADMIN" -> systemAdmin;
                case "SPECIAL_USER" -> specialUser;
                default -> userRole;
            };
            userRoleRepository.save(new UserRole(user, mapped));
        }
    }

    private String findLegacyRole(Long userId) {
        try {
            List<String> roles = jdbcTemplate.query(
                    "SELECT role FROM users WHERE id = ?",
                    (rs, rowNum) -> rs.getString("role"),
                    userId
            );
            return roles.isEmpty() ? null : roles.get(0);
        } catch (Exception ex) {
            return null;
        }
    }

    private void ensureDefaultMenus(Role systemAdmin, Role specialUser, Role userRole) {
        if (!menuRepository.findAll().isEmpty()) {
            return;
        }

        createMenu("대시보드", "/", 1, null, List.of(systemAdmin, specialUser, userRole), true, false, false, false, false, false);
        createMenu("특별 사용자", "/special", 2, null, List.of(systemAdmin, specialUser), true, true, false, false, false, false);

        Menu adminFolder = createMenu("시스템 관리", null, 3, null, List.of(), false, false, false, false, false, false);
        createMenu("사용자 Role 관리", "/admin/users", 1, adminFolder, List.of(systemAdmin), true, true, false, false, false, false);
        createMenu("Role 관리", "/admin/roles", 2, adminFolder, List.of(systemAdmin), true, true, true, false, false, false);
        createMenu("메뉴 관리", "/admin/menus", 3, adminFolder, List.of(systemAdmin), true, true, true, false, false, false);
    }

    private Menu createMenu(String name, String url, int sortOrder, Menu parent, List<Role> roles,
                            boolean canRead, boolean canUpdate, boolean canDelete,
                            boolean canUpload, boolean canDownload, boolean canOther) {
        Menu menu = menuRepository.save(new Menu(name, url, sortOrder, parent));
        for (Role role : roles) {
            menuRoleRepository.save(new MenuRole(menu, role));
            menuRoleButtonRepository.save(new MenuRoleButton(
                    menu, role, canRead, canUpdate, canDelete, canUpload, canDownload, canOther
            ));
        }
        return menu;
    }

    private void migrateSchema() {
        try {
            jdbcTemplate.execute("ALTER TABLE users DROP COLUMN IF EXISTS role");
        } catch (Exception ignored) {
            // ignore if already dropped or unsupported
        }
        try {
            jdbcTemplate.execute("ALTER TABLE roles DROP COLUMN IF EXISTS system_role");
        } catch (Exception ignored) {
            // ignore if already dropped or unsupported
        }
        try {
            // 폴더 메뉴(url null)를 위해 기존 NOT NULL 제약 제거
            // Hibernate ddl-auto=update 는 NOT NULL 해제를 자동으로 하지 않음
            jdbcTemplate.execute("ALTER TABLE menus ALTER COLUMN url DROP NOT NULL");
        } catch (Exception ignored) {
            // ignore if already nullable
        }
    }
}
