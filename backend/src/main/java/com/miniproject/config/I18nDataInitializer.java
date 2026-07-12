package com.miniproject.config;

import com.miniproject.domain.I18nLocale;
import com.miniproject.domain.I18nLocaleRepository;
import com.miniproject.domain.I18nMessage;
import com.miniproject.domain.I18nMessageGroup;
import com.miniproject.domain.I18nMessageGroupRepository;
import com.miniproject.domain.I18nMessageRepository;
import com.miniproject.domain.I18nMessageText;
import com.miniproject.domain.I18nMessageTextRepository;
import com.miniproject.domain.Menu;
import com.miniproject.domain.MenuRepository;
import com.miniproject.domain.MenuRole;
import com.miniproject.domain.MenuRoleButton;
import com.miniproject.domain.MenuRoleButtonRepository;
import com.miniproject.domain.MenuRoleRepository;
import com.miniproject.domain.Role;
import com.miniproject.domain.RoleRepository;
import com.miniproject.service.RoleService;
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

        Menu folder = menuRepository.save(new Menu("다국어 관리", null, 90, null));
        createLeaf(folder, "로케일 관리", "/admin/i18n/locales", 1, systemAdmin);
        createLeaf(folder, "메시지 그룹", "/admin/i18n/groups", 2, systemAdmin);
        createLeaf(folder, "메시지 관리", "/admin/i18n/messages", 3, systemAdmin);
    }

    private void createLeaf(Menu parent, String name, String url, int sortOrder, Role role) {
        Menu menu = menuRepository.save(new Menu(name, url, sortOrder, parent));
        menuRoleRepository.save(new MenuRole(menu, role));
        menuRoleButtonRepository.save(new MenuRoleButton(
                menu, role, true, true, true, false, false, false
        ));
    }
}
