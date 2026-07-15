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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
    @Order(Ordered.LOWEST_PRECEDENCE)
    @Transactional
    public void seed() {
        I18nLocale ko = ensureLocale("ko", "한국어", 1);
        I18nLocale en = ensureLocale("en", "English", 2);

        I18nMessageGroup common = ensureGroup("common", "공통", "공통 메시지");
        I18nMessageGroup auth = ensureGroup("auth", "인증", "로그인/회원가입 메시지");
        I18nMessageGroup menu = ensureGroup("menu", "메뉴", "사이드바 메뉴명");
        I18nMessageGroup table = ensureGroup("table", "테이블", "테이블 컬럼 헤더");

        ensureMessage(common, "welcome", "환영 인사",
                ko, "안녕하세요, {name}님!",
                en, "Hello, {name}!");
        ensureMessage(common, "itemCount", "항목 수",
                ko, "{0}개 중 {1}개",
                en, "{1} of {0}");
        ensureMessage(common, "changePassword", "비밀번호 변경",
                ko, "비밀번호 변경",
                en, "Change Password");
        ensureMessage(common, "logout", "로그아웃",
                ko, "로그아웃",
                en, "Logout");
        ensureMessage(common, "refresh", "새로고침",
                ko, "새로고침",
                en, "Refresh");
        ensureMessage(common, "user", "사용자",
                ko, "사용자",
                en, "User");
        ensureMessage(common, "hideMenu", "메뉴 숨기기",
                ko, "메뉴 숨기기",
                en, "Hide menu");
        ensureMessage(common, "showMenu", "메뉴 보이기",
                ko, "메뉴 보이기",
                en, "Show menu");
        ensureMessage(auth, "loginSuccess", "로그인 성공",
                ko, "로그인되었습니다.",
                en, "You have signed in.");
        ensureMessage(auth, "loginFailed", "로그인 실패",
                ko, "이메일 또는 비밀번호가 올바르지 않습니다.",
                en, "Invalid email or password.");

        seedMenuMessages(menu, ko, en);
        seedTableMessages(table, ko, en);

        ensureI18nMenus();
        ensureMenuNameI18nKeys();
    }

    private void seedMenuMessages(I18nMessageGroup menu, I18nLocale ko, I18nLocale en) {
        ensureMessage(menu, "dashboard", "대시보드", ko, "대시보드", en, "Dashboard");
        ensureMessage(menu, "special", "특별 사용자", ko, "특별 사용자", en, "Special");
        ensureMessage(menu, "system", "시스템 관리", ko, "시스템 관리", en, "System");
        ensureMessage(menu, "users", "사용자 관리", ko, "사용자 관리", en, "Users");
        ensureMessage(menu, "roles", "Role 관리", ko, "Role 관리", en, "Roles");
        ensureMessage(menu, "menus", "메뉴 관리", ko, "메뉴 관리", en, "Menus");
        ensureMessage(menu, "i18n", "다국어 관리", ko, "다국어 관리", en, "i18n");
        ensureMessage(menu, "locales", "로케일 관리", ko, "로케일 관리", en, "Locales");
        ensureMessage(menu, "groups", "메시지 그룹", ko, "메시지 그룹", en, "Message Groups");
        ensureMessage(menu, "messages", "메시지 관리", ko, "메시지 관리", en, "Messages");
        ensureMessage(menu, "mailTemplates", "메일 템플릿", ko, "메일 템플릿", en, "Mail Templates");
        ensureMessage(menu, "settings", "시스템 설정", ko, "시스템 설정", en, "Settings");
        ensureMessage(menu, "menuAccessLogs", "메뉴 접근 이력", ko, "메뉴 접근 이력", en, "Menu Access Logs");
        ensureMessage(menu, "demo", "반응형 테스트", ko, "반응형 테스트", en, "Responsive Demo");
        ensureMessage(menu, "demoSearch", "검색 화면", ko, "검색 화면", en, "Search");
        ensureMessage(menu, "demoView", "조회 화면", ko, "조회 화면", en, "View");
        ensureMessage(menu, "demoEdit", "수정 화면", ko, "수정 화면", en, "Edit");
        ensureMessage(menu, "demoPopup", "팝업 테스트", ko, "팝업 테스트", en, "Popup");
    }

    private void seedTableMessages(I18nMessageGroup table, I18nLocale ko, I18nLocale en) {
        ensureMessage(table, "id", "ID", ko, "ID", en, "ID");
        ensureMessage(table, "name", "이름", ko, "이름", en, "Name");
        ensureMessage(table, "email", "이메일", ko, "이메일", en, "Email");
        ensureMessage(table, "code", "코드", ko, "코드", en, "Code");
        ensureMessage(table, "description", "설명", ko, "설명", en, "Description");
        ensureMessage(table, "status", "상태", ko, "상태", en, "Status");
        ensureMessage(table, "manage", "관리", ko, "관리", en, "Manage");
        ensureMessage(table, "actions", "작업", ko, "작업", en, "Actions");
        ensureMessage(table, "role", "Role", ko, "Role", en, "Role");
        ensureMessage(table, "createdAt", "가입일", ko, "가입일", en, "Joined");
        ensureMessage(table, "activeToggle", "활성/비활성", ko, "활성/비활성", en, "Enable");
        ensureMessage(table, "enabled", "사용", ko, "사용", en, "Enabled");
        ensureMessage(table, "sortOrder", "정렬", ko, "정렬", en, "Order");
        ensureMessage(table, "group", "그룹", ko, "그룹", en, "Group");
        ensureMessage(table, "translation", "번역", ko, "번역", en, "Translation");
        ensureMessage(table, "from", "발신자", ko, "발신자", en, "From");
        ensureMessage(table, "to", "수신자", ko, "수신자", en, "To");
        ensureMessage(table, "subject", "제목", ko, "제목", en, "Subject");
        ensureMessage(table, "accessedAt", "접근 시각", ko, "접근 시각", en, "Accessed At");
        ensureMessage(table, "user", "사용자", ko, "사용자", en, "User");
        ensureMessage(table, "type", "유형", ko, "유형", en, "Type");
        ensureMessage(table, "typeMenu", "유형-메뉴", ko, "메뉴", en, "Menu");
        ensureMessage(table, "typeApi", "유형-API", ko, "API", en, "API");
        ensureMessage(table, "menuName", "메뉴명", ko, "메뉴명", en, "Menu Name");
        ensureMessage(table, "menuUrl", "메뉴 URL", ko, "메뉴 URL", en, "Menu URL");
        ensureMessage(table, "httpMethod", "Method", ko, "Method", en, "Method");
        ensureMessage(table, "requestUri", "Request URI", ko, "Request URI", en, "Request URI");
        ensureMessage(table, "clientIp", "IP", ko, "IP", en, "IP");
        ensureMessage(table, "httpStatus", "Status", ko, "Status", en, "Status");
        ensureMessage(table, "productName", "상품명", ko, "상품명", en, "Product");
        ensureMessage(table, "category", "카테고리", ko, "카테고리", en, "Category");
        ensureMessage(table, "price", "가격", ko, "가격", en, "Price");
        ensureMessage(table, "stock", "재고", ko, "재고", en, "Stock");
        ensureMessage(table, "owner", "담당자", ko, "담당자", en, "Owner");
        ensureMessage(table, "updatedAt", "수정일", ko, "수정일", en, "Updated");
        ensureMessage(table, "canRead", "조회", ko, "조회", en, "Read");
        ensureMessage(table, "canUpdate", "수정", ko, "수정", en, "Update");
        ensureMessage(table, "canDelete", "삭제", ko, "삭제", en, "Delete");
        ensureMessage(table, "canUpload", "업로드", ko, "업로드", en, "Upload");
        ensureMessage(table, "canDownload", "다운로드", ko, "다운로드", en, "Download");
        ensureMessage(table, "canOther", "기타", ko, "기타", en, "Other");
        ensureMessage(table, "key", "키", ko, "키", en, "Key");
        ensureMessage(table, "preview", "미리보기", ko, "미리보기", en, "Preview");
        ensureMessage(table, "select", "선택", ko, "선택", en, "Select");
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
                .ifPresentOrElse(existing -> {
                    if (!text.equals(existing.getText())) {
                        existing.changeText(text);
                        textRepository.save(existing);
                    }
                }, () -> textRepository.save(new I18nMessageText(message, locale, text)));
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

    /** 기존/신규 메뉴에 nameI18nKey를 연결 (재기동 시에도 누락분 보정) */
    private void ensureMenuNameI18nKeys() {
        bindMenuByUrl("/", "menu.dashboard");
        bindMenuByUrl("/special", "menu.special");
        bindMenuByUrl("/admin/users", "menu.users");
        bindMenuByUrl("/admin/roles", "menu.roles");
        bindMenuByUrl("/admin/menus", "menu.menus");
        bindMenuByUrl("/admin/i18n/locales", "menu.locales");
        bindMenuByUrl("/admin/i18n/groups", "menu.groups");
        bindMenuByUrl("/admin/i18n/messages", "menu.messages");
        bindMenuByUrl("/admin/mail/templates", "menu.mailTemplates");
        bindMenuByUrl("/admin/settings", "menu.settings");
        bindMenuByUrl("/admin/menu-access-logs", "menu.menuAccessLogs");
        bindMenuByUrl("/demo/search", "menu.demoSearch");
        bindMenuByUrl("/demo/view", "menu.demoView");
        bindMenuByUrl("/demo/edit", "menu.demoEdit");
        bindMenuByUrl("/demo/popup", "menu.demoPopup");

        bindMenuFolderByName("시스템 관리", "menu.system");
        bindMenuFolderByName("다국어 관리", "menu.i18n");
        bindMenuFolderByName("반응형 테스트", "menu.demo");
        // renamed leaf still present in some DBs
        bindMenuFolderByName("사용자 Role 관리", "menu.users");
    }

    private void bindMenuByUrl(String url, String i18nKey) {
        menuRepository.findByUrl(url).ifPresent(menu -> applyI18nKey(menu, i18nKey));
    }

    private void bindMenuFolderByName(String name, String i18nKey) {
        for (Menu menu : menuRepository.findAll()) {
            if (menu.getUrl() == null && name.equals(menu.getName())) {
                applyI18nKey(menu, i18nKey);
            }
        }
    }

    private void applyI18nKey(Menu menu, String i18nKey) {
        if (i18nKey.equals(menu.getNameI18nKey())) {
            return;
        }
        menu.changeNameI18nKey(i18nKey);
        menuRepository.save(menu);
    }
}
