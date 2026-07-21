# 메뉴 관리 기능 정의서

| 항목 | 내용 |
|------|------|
| 문서 버전 | 1.0 |
| 작성일 | 2026-07-21 |
| 대상 시스템 | miniproject (Vue 3 + Spring Boot) |
| 상태 | **확정** (구현 기준) |

---

## 1. 목적

사이드바 **메뉴 트리**, Role별 접근, **버튼 권한**(read/update/delete/upload/download/other)을 관리한다.

---

## 2. 메뉴·경로

| 항목 | 값 |
|------|-----|
| 폴더 | 시스템 관리 |
| 메뉴명 | 메뉴 관리 |
| URL | `/admin/menus` |
| i18n key | `menu.menus` |
| 접근 | `SYSTEM_ADMIN` |
| 버튼 | 시드: `canRead`, `canUpdate`, `canDelete` |

---

## 3. 기능 범위

| ID | 기능 | 설명 |
|----|------|------|
| M-01 | 트리 조회 | 폴더·리프 계층 |
| M-02 | 생성/수정 | 이름, URL, 정렬, 부모, 활성, `nameI18nKey` |
| M-03 | 삭제 | 메뉴 및 Role·버튼 매핑 삭제 |
| M-04 | 순서 변경 | 드래그/reorder API |
| M-05 | Role 할당 | 리프 메뉴별 Role |
| M-06 | 버튼 권한 | Role별 can* 플래그 |

### 런타임(사용자)

| ID | 기능 | 설명 |
|----|------|------|
| M-10 | 내 메뉴 | `GET /api/menus/my` — 활성 경로의 접근 가능 리프(+폴더) |
| M-11 | 접근 로그 트리거 | 화면 진입 시 `POST /api/menus/access` |

---

## 4. 비즈니스 규칙

1. URL 있는 노드 = **리프**, URL 없음 = **폴더**.
2. 리프 URL **고유**.
3. 순환 부모 지정 불가. 자식 있는 폴더를 리프로 전환 불가.
4. 비활성 메뉴(또는 경로상 비활성)는 내 메뉴에서 제외.
5. 접근 판정은 **리프 URL** 기준. 폴더는 자식이 있을 때 네비게이션용으로 포함.
6. 프론트 라우트 `requiresMenu` + (선택) `menuAnyOf`로 화면 게이트.

---

## 5. API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/admin/menus` | 전체 트리(관리) |
| POST | `/api/admin/menus` | 생성 |
| PUT | `/api/admin/menus/{menuId}` | 수정 |
| PUT | `/api/admin/menus/reorder` | 순서 |
| DELETE | `/api/admin/menus/{menuId}` | 삭제 |
| GET | `/api/menus/my` | 내 메뉴 |
| POST | `/api/menus/access` | 메뉴 접근 기록용 |

---

## 6. 데이터

- `pjt_menus` (`enabled`, `name_i18n_key` 등)
- `pjt_menu_roles`, `pjt_menu_role_buttons`

---

## 7. 수용 기준

- [ ] 폴더/리프 CRUD·순서 변경
- [ ] Role·버튼 권한 저장 후 사용자 메뉴·`v-can` 반영
- [ ] 비활성 메뉴 미노출
- [ ] 권한 없는 URL → 403

---

## 8. 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 1.0 | 2026-07-21 | 초안(구현 반영) |
