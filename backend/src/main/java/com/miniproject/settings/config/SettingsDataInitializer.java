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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SettingsDataInitializer {

    private final SystemSettingsService systemSettingsService;
    private final MenuRepository menuRepository;
    private final MenuRoleRepository menuRoleRepository;
    private final MenuRoleButtonRepository menuRoleButtonRepository;
    private final RoleRepository roleRepository;
    private final JdbcTemplate jdbcTemplate;

    public SettingsDataInitializer(SystemSettingsService systemSettingsService,
                                   MenuRepository menuRepository,
                                   MenuRoleRepository menuRoleRepository,
                                   MenuRoleButtonRepository menuRoleButtonRepository,
                                   RoleRepository roleRepository,
                                   JdbcTemplate jdbcTemplate) {
        this.systemSettingsService = systemSettingsService;
        this.menuRepository = menuRepository;
        this.menuRoleRepository = menuRoleRepository;
        this.menuRoleButtonRepository = menuRoleButtonRepository;
        this.roleRepository = roleRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        migrateDefaultRoleCodesColumn();
        systemSettingsService.getOrCreate();
        ensureSettingsMenu();
    }

    private void migrateDefaultRoleCodesColumn() {
        if (!tableExists("system_settings")) {
            return;
        }

        jdbcTemplate.execute("""
                ALTER TABLE system_settings
                ADD COLUMN IF NOT EXISTS default_role_codes VARCHAR(500)
                """);

        if (columnExists("system_settings", "default_role_code")) {
            jdbcTemplate.execute("""
                    UPDATE system_settings
                    SET default_role_codes = default_role_code
                    WHERE default_role_codes IS NULL
                      AND default_role_code IS NOT NULL
                    """);
            jdbcTemplate.execute("ALTER TABLE system_settings DROP COLUMN IF EXISTS default_role_code");
        }

        jdbcTemplate.execute("""
                UPDATE system_settings
                SET default_role_codes = 'USER'
                WHERE default_role_codes IS NULL OR TRIM(default_role_codes) = ''
                """);

        Boolean notNull = jdbcTemplate.queryForObject("""
                SELECT is_nullable = 'NO'
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'system_settings'
                  AND column_name = 'default_role_codes'
                """, Boolean.class);
        if (!Boolean.TRUE.equals(notNull)) {
            jdbcTemplate.execute("""
                    ALTER TABLE system_settings
                    ALTER COLUMN default_role_codes SET NOT NULL
                    """);
        }
    }

    private boolean tableExists(String tableName) {
        Boolean exists = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                  SELECT 1
                  FROM information_schema.tables
                  WHERE table_schema = 'public' AND table_name = ?
                )
                """, Boolean.class, tableName);
        return Boolean.TRUE.equals(exists);
    }

    private boolean columnExists(String tableName, String columnName) {
        Boolean exists = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                  SELECT 1
                  FROM information_schema.columns
                  WHERE table_schema = 'public'
                    AND table_name = ?
                    AND column_name = ?
                )
                """, Boolean.class, tableName, columnName);
        return Boolean.TRUE.equals(exists);
    }

    private void ensureSettingsMenu() {
        if (menuRepository.existsByUrl("/admin/settings")) {
            return;
        }
        Role systemAdmin = roleRepository.findByCode(RoleService.SYSTEM_ADMIN_CODE).orElse(null);
        if (systemAdmin == null) {
            return;
        }
        Menu menu = menuRepository.save(new Menu("시스템 설정", "/admin/settings", 100, null));
        menu.changeNameI18nKey("menu.settings");
        menuRoleRepository.save(new MenuRole(menu, systemAdmin));
        menuRoleButtonRepository.save(new MenuRoleButton(
                menu, systemAdmin, true, true, false, false, false, false
        ));
    }
}
