# miniproject 기능 정의서

| 문서 | 메뉴 / 범위 | 비고 |
|------|-------------|------|
| [dashboard-feature-spec.md](./dashboard-feature-spec.md) | 대시보드 | |
| [special-feature-spec.md](./special-feature-spec.md) | 특별 사용자 | |
| [admin-users-feature-spec.md](./admin-users-feature-spec.md) | 시스템 관리 › 사용자 Role 관리 | |
| [admin-roles-feature-spec.md](./admin-roles-feature-spec.md) | 시스템 관리 › Role 관리 | |
| [admin-menus-feature-spec.md](./admin-menus-feature-spec.md) | 시스템 관리 › 메뉴 관리 | |
| [menu-access-logs-feature-spec.md](./menu-access-logs-feature-spec.md) | 메뉴 접근 이력 | |
| [i18n-feature-spec.md](./i18n-feature-spec.md) | 다국어 관리 (로케일·그룹·메시지) | |
| [mail-templates-feature-spec.md](./mail-templates-feature-spec.md) | 메일 템플릿 | |
| [system-settings-feature-spec.md](./system-settings-feature-spec.md) | 시스템 설정 | |
| [approval-feature-spec.md](./approval-feature-spec.md) | 결재 (함·기안·엔진) | |

## 제외

- **반응형 테스트** (`/demo/*`) — 데모·레이아웃 검증용. 기능정의서 작성 대상 아님.

## 공통 전제

| 항목 | 내용 |
|------|------|
| 스택 | Vue 3 + Spring Boot |
| 인증 | JWT access + HttpOnly refresh |
| 권한 | 메뉴(`pjt_menus`) + Role + 버튼 권한(`pjt_menu_role_buttons`) |
| 관리 API | `/api/admin/**` → `SYSTEM_ADMIN` 필수 |
| 시드 Role | `SYSTEM_ADMIN`, `SPECIAL_USER`, `USER` |

작성일 기준: 2026-07-21
