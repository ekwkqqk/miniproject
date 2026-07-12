package com.miniproject.mail.config;

import com.miniproject.mail.domain.MailTemplate;
import com.miniproject.mail.domain.MailTemplateRepository;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableConfigurationProperties(MailAppProperties.class)
public class MailDataInitializer {

    private final MailTemplateRepository mailTemplateRepository;
    private final MenuRepository menuRepository;
    private final MenuRoleRepository menuRoleRepository;
    private final MenuRoleButtonRepository menuRoleButtonRepository;
    private final RoleRepository roleRepository;

    public MailDataInitializer(MailTemplateRepository mailTemplateRepository,
                               MenuRepository menuRepository,
                               MenuRoleRepository menuRoleRepository,
                               MenuRoleButtonRepository menuRoleButtonRepository,
                               RoleRepository roleRepository) {
        this.mailTemplateRepository = mailTemplateRepository;
        this.menuRepository = menuRepository;
        this.menuRoleRepository = menuRoleRepository;
        this.menuRoleButtonRepository = menuRoleButtonRepository;
        this.roleRepository = roleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (!mailTemplateRepository.existsByCode("welcome")) {
            mailTemplateRepository.save(new MailTemplate(
                    "welcome",
                    "가입 환영 메일",
                    "회원가입 환영 안내",
                    "noreply@example.com",
                    "miniproject",
                    "user@example.com",
                    null,
                    null,
                    "[miniproject] {name}님, 환영합니다",
                    """
                    <p>안녕하세요, <strong>{name}</strong>님!</p>
                    <p>miniproject에 가입해 주셔서 감사합니다.</p>
                    """,
                    true,
                    true
            ));
        }
        ensureMailMenu();
    }

    private void ensureMailMenu() {
        if (menuRepository.existsByUrl("/admin/mail/templates")) {
            return;
        }
        Role systemAdmin = roleRepository.findByCode(RoleService.SYSTEM_ADMIN_CODE).orElse(null);
        if (systemAdmin == null) {
            return;
        }
        Menu menu = menuRepository.save(new Menu("메일 템플릿", "/admin/mail/templates", 95, null));
        menuRoleRepository.save(new MenuRole(menu, systemAdmin));
        menuRoleButtonRepository.save(new MenuRoleButton(
                menu, systemAdmin, true, true, true, false, false, false
        ));
    }
}
