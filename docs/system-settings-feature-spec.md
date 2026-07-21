# 시스템 설정 기능 정의서

| 항목 | 내용 |
|------|------|
| 문서 버전 | 1.0 |
| 작성일 | 2026-07-21 |
| 대상 시스템 | miniproject (Vue 3 + Spring Boot) |
| 상태 | **확정** (구현 기준) |

---

## 1. 목적

전역 **싱글톤 설정**(테마·비밀번호 정책·기본 Role·로그인 정책)을 관리한다.

---

## 2. 메뉴·경로

| 항목 | 값 |
|------|-----|
| 메뉴명 | 시스템 설정 |
| URL | `/admin/settings` |
| i18n key | `menu.settings` |
| 접근 | `SYSTEM_ADMIN` (수정은 메뉴 `canUpdate`) |

---

## 3. 설정 항목

| 항목 | 설명 | 영향 |
|------|------|------|
| theme primary color | UI 테마 색 | 공개 설정으로 프론트 반영 |
| password change period (days) | 비밀번호 변경 주기 | 로그인 시 만료 → 변경 화면 |
| password min length | 최소 길이 | 가입·생성·변경·초기화 |
| default role codes | 신규 사용자 기본 Role | 등록·관리자 생성 |
| allow multi-login | 다중 로그인 허용 | false면 로그인 시 타 세션 폐기 |
| max failed login attempts | 실패 허용 횟수 | 0이면 잠금 없음, 초과 시 계정 잠금 |

---

## 4. 비즈니스 규칙

1. 설정 행은 **id=1 싱글톤**.
2. default role codes **1개 이상**, 존재하는 Role code만.
3. 공개 API는 테마 등 비민감 값만 노출.

---

## 5. API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/admin/settings` | 전체(관리) |
| PUT | `/api/admin/settings` | 저장 |
| GET | `/api/settings/public` | 공개(테마 등) |

---

## 6. 데이터

- `pjt_system_settings` (+ `max_failed_login_attempts` 등)

---

## 7. 수용 기준

- [ ] 관리자만 수정
- [ ] 비밀번호 길이·주기·실패횟수가 인증 흐름에 반영
- [ ] 기본 Role이 사용자 생성에 반영
- [ ] 공개 API로 테마 로드

---

## 8. 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 1.0 | 2026-07-21 | 초안(구현 반영) |
