package com.miniproject.auth.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * IdP(SSO/OAuth/OIDC)에서 검증을 마친 뒤 앱에 넘기는 외부 신원 정보.
 * <p>
 * 이 객체의 값은 반드시 IdP 토큰/클레임을 서버에서 검증한 결과여야 한다.
 * 클라이언트가 보낸 raw claim을 그대로 넣으면 안 된다.
 */
public class ExternalIdentity {

    private final String email;
    private final String name;
    /** IdP subject (sub). 매핑용으로 보관하며, 현재는 인가에 직접 사용하지 않는다. */
    private final String subject;
    /** IdP에서 내려준 역할 코드. 비어 있으면 시스템 기본 Role을 사용한다. */
    private final List<String> roleCodes;
    /**
     * true이면 매 로그인마다 roleCodes로 사용자 Role을 재동기화한다.
     * false(기본)이면 신규 사용자 생성 시에만 Role을 부여하고, 기존 사용자는 DB Role을 유지한다.
     */
    private final boolean syncRoles;

    public ExternalIdentity(String email, String name, String subject, List<String> roleCodes, boolean syncRoles) {
        this.email = email;
        this.name = name;
        this.subject = subject;
        this.roleCodes = roleCodes == null ? List.of() : List.copyOf(roleCodes);
        this.syncRoles = syncRoles;
    }

    public static ExternalIdentity of(String email, String name) {
        return new ExternalIdentity(email, name, null, Collections.emptyList(), false);
    }

    public static ExternalIdentity of(String email, String name, String subject) {
        return new ExternalIdentity(email, name, subject, Collections.emptyList(), false);
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public boolean isSyncRoles() {
        return syncRoles;
    }

    public ExternalIdentity withRoleCodes(List<String> codes, boolean sync) {
        return new ExternalIdentity(email, name, subject, codes, sync);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalIdentity that)) return false;
        return syncRoles == that.syncRoles
                && Objects.equals(email, that.email)
                && Objects.equals(name, that.name)
                && Objects.equals(subject, that.subject)
                && Objects.equals(roleCodes, that.roleCodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, name, subject, roleCodes, syncRoles);
    }
}
