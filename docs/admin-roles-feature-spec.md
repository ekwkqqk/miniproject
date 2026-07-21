# Role 관리 기능 정의서

| 항목 | 내용 |
|------|------|
| 문서 버전 | 1.0 |
| 작성일 | 2026-07-21 |
| 대상 시스템 | miniproject (Vue 3 + Spring Boot) |
| 상태 | **확정** (구현 기준) |

---

## 1. 목적

시스템관리자가 **Role 코드·이름·설명**을 등록·삭제하여 메뉴·사용자 권한의 기준을 관리한다.

---

## 2. 메뉴·경로

| 항목 | 값 |
|------|-----|
| 폴더 | 시스템 관리 |
| 메뉴명 | Role 관리 |
| URL | `/admin/roles` |
| i18n key | `menu.roles` |
| 접근 | `SYSTEM_ADMIN` |
| 버튼 | 시드: `canRead`, `canUpdate`, `canDelete` |

---

## 3. 기능 범위

| ID | 기능 | 설명 |
|----|------|------|
| R-01 | 목록 | Role 목록 |
| R-02 | 생성 | code / name / description |
| R-03 | 삭제 | Role 삭제 및 연관 매핑 정리 |

### 제외

- Role 수정(UPDATE) API (현재 미제공)
- Role 코드 변경

---

## 4. 비즈니스 규칙

1. `code`는 대문자 정규화, **고유**.
2. 삭제 시 연쇄: `pjt_user_roles`, `pjt_menu_roles`, `pjt_menu_role_buttons` 해당 Role 행 제거.
3. 시드 Role: `SYSTEM_ADMIN`, `SPECIAL_USER`, `USER` (운영 중 삭제 시 주의).

---

## 5. API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/admin/roles` | 목록 |
| POST | `/api/admin/roles` | 생성 |
| DELETE | `/api/admin/roles/{roleId}` | 삭제 |

---

## 6. 데이터

- `pjt_roles` (+ 연관 매핑 테이블)

---

## 7. 수용 기준

- [ ] 관리자만 접근
- [ ] 중복 code 생성 거부
- [ ] 삭제 후 해당 Role 메뉴·사용자 매핑 제거

---

## 8. 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 1.0 | 2026-07-21 | 초안(구현 반영) |
