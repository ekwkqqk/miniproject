package com.miniproject.i18n.config;

import com.miniproject.i18n.domain.I18nLocale;
import com.miniproject.i18n.domain.I18nLocaleRepository;
import com.miniproject.i18n.domain.I18nMessage;
import com.miniproject.i18n.domain.I18nMessageGroup;
import com.miniproject.i18n.domain.I18nMessageGroupRepository;
import com.miniproject.i18n.domain.I18nMessageRepository;
import com.miniproject.i18n.domain.I18nMessageText;
import com.miniproject.i18n.domain.I18nMessageTextRepository;
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
public class I18nDataInitializer {

    private final I18nLocaleRepository localeRepository;
    private final I18nMessageGroupRepository groupRepository;
    private final I18nMessageRepository messageRepository;
    private final I18nMessageTextRepository textRepository;
    private final MenuRepository menuRepository;
    private final MenuRoleRepository menuRoleRepository;
    private final MenuRoleButtonRepository menuRoleButtonRepository;
    private final RoleRepository roleRepository;

    public I18nDataInitializer(I18nLocaleRepository localeRepository,
                               I18nMessageGroupRepository groupRepository,
                               I18nMessageRepository messageRepository,
                               I18nMessageTextRepository textRepository,
                               MenuRepository menuRepository,
                               MenuRoleRepository menuRoleRepository,
                               MenuRoleButtonRepository menuRoleButtonRepository,
                               RoleRepository roleRepository) {
        this.localeRepository = localeRepository;
        this.groupRepository = groupRepository;
        this.messageRepository = messageRepository;
        this.textRepository = textRepository;
        this.menuRepository = menuRepository;
        this.menuRoleRepository = menuRoleRepository;
        this.menuRoleButtonRepository = menuRoleButtonRepository;
        this.roleRepository = roleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        I18nLocale ko = ensureLocale("ko", "한국어", 1);
        I18nLocale en = ensureLocale("en", "English", 2);

        I18nMessageGroup common = ensureGroup("common", "공통", "공통 메시지");
        I18nMessageGroup auth = ensureGroup("auth", "인증", "로그인/회원가입 메시지");

        ensureMessage(common, "welcome", "환영 인사",
                ko, "안녕하세요, {name}님!",
                en, "Hello, {name}!");
        ensureMessage(common, "itemCount", "항목 수",
                ko, "{0}개 중 {1}개",
                en, "{1} of {0}");
        ensureMessage(auth, "loginSuccess", "로그인 성공",
                ko, "로그인되었습니다.",
                en, "You have signed in.");
        ensureMessage(auth, "loginFailed", "로그인 실패",
                ko, "이메일 또는 비밀번호가 올바르지 않습니다.",
                en, "Invalid email or password.");

        I18nMessageGroup menu = ensureGroup("menu", "메뉴", "사이드바 메뉴명");
        ensureMessage(menu, "dashboard", "대시보드",
                ko, "대시보드",
                en, "Dashboard");
        ensureMessage(menu, "special", "특별 사용자",
                ko, "특별 사용자",
                en, "Special");
        ensureMessage(menu, "system", "시스템 관리",
                ko, "시스템 관리",
                en, "System");
        ensureMessage(menu, "users", "사용자 Role 관리",
                ko, "사용자 Role 관리",
                en, "User Roles");
        ensureMessage(menu, "roles", "Role 관리",
                ko, "Role 관리",
                en, "Roles");
        ensureMessage(menu, "menus", "메뉴 관리",
                ko, "메뉴 관리",
                en, "Menus");
        ensureMessage(menu, "i18n", "다국어 관리",
                ko, "다국어 관리",
                en, "i18n");
        ensureMessage(menu, "locales", "로케일 관리",
                ko, "로케일 관리",
                en, "Locales");
        ensureMessage(menu, "groups", "메시지 그룹",
                ko, "메시지 그룹",
                en, "Message Groups");
        ensureMessage(menu, "messages", "메시지 관리",
                ko, "메시지 관리",
                en, "Messages");
        ensureMessage(menu, "mailTemplates", "메일 템플릿",
                ko, "메일 템플릿",
                en, "Mail Templates");
        ensureMessage(menu, "settings", "시스템 설정",
                ko, "시스템 설정",
                en, "Settings");

        ensureI18nMenus();
    }

    private I18nLocale ensureLocale(String code, String name, int sortOrder) {
        return localeRepository.findByCode(code)
                .orElseGet(() -> localeRepository.save(new I18nLocale(code, name, true, sortOrder)));
    }

    private I18nMessageGroup ensureGroup(String code, String name, String description) {
        return groupRepository.findByCode(code)
                .orElseGet(() -> groupRepository.save(new I18nMessageGroup(code, name, description)));
    }

    private void ensureMessage(I18nMessageGroup group, String code, String description,
                               I18nLocale locale1, String text1,
                               I18nLocale locale2, String text2) {
        I18nMessage message = messageRepository.findByGroupIdAndCode(group.getId(), code)
                .orElseGet(() -> messageRepository.save(new I18nMessage(group, code, description)));
        ensureText(message, locale1, text1);
        ensureText(message, locale2, text2);
    }

    private void ensureText(I18nMessage message, I18nLocale locale, String text) {
        textRepository.findByMessageIdAndLocaleId(message.getId(), locale.getId())
                .orElseGet(() -> textRepository.save(new I18nMessageText(message, locale, text)));
    }

    private void ensureI18nMenus() {
        if (menuRepository.existsByUrl("/admin/i18n/locales")) {
            return;
        }
        Role systemAdmin = roleRepository.findByCode(RoleService.SYSTEM_ADMIN_CODE).orElse(null);
        if (systemAdmin == null) {
            return;
        }

        Menu folder = new Menu("다국어 관리", null, 90, null);
        folder.changeNameI18nKey("menu.i18n");
        folder = menuRepository.save(folder);
        createLeaf(folder, "로케일 관리", "/admin/i18n/locales", 1, systemAdmin);
        createLeaf(folder, "메시지 그룹", "/admin/i18n/groups", 2, systemAdmin);
        createLeaf(folder, "메시지 관리", "/admin/i18n/messages", 3, systemAdmin);
    }

    private void createLeaf(Menu parent, String name, String url, int sortOrder, Role role) {
        Menu menu = new Menu(name, url, sortOrder, parent);
        String i18nKey = switch (url) {
            case "/admin/i18n/locales" -> "menu.locales";
            case "/admin/i18n/groups" -> "menu.groups";
            case "/admin/i18n/messages" -> "menu.messages";
            default -> null;
        };
        if (i18nKey != null) {
            menu.changeNameI18nKey(i18nKey);
        }
        menu = menuRepository.save(menu);
        menuRoleRepository.save(new MenuRole(menu, role));
        menuRoleButtonRepository.save(new MenuRoleButton(
                menu, role, true, true, true, false, false, false
        ));
    }
}
