# 메뉴 접근 이력 기능 정의서

| 항목 | 내용 |
|------|------|
| 문서 버전 | 1.0 |
| 작성일 | 2026-07-21 |
| 대상 시스템 | miniproject (Vue 3 + Spring Boot) |
| 상태 | **확정** (구현 기준) |

---

## 1. 목적

인증 사용자의 **메뉴 화면 진입** 및 **관리 API 호출**을 이력으로 남겨 감사·추적에 사용한다.

---

## 2. 메뉴·경로

| 항목 | 값 |
|------|-----|
| 메뉴명 | 메뉴 접근 이력 |
| URL | `/admin/menu-access-logs` |
| i18n key | `menu.menuAccessLogs` |
| 접근 | `SYSTEM_ADMIN` (조회 전용) |

---

## 3. 기능 범위

| ID | 기능 | 설명 |
|----|------|------|
| L-01 | 이력 조회 | 페이지네이션 목록 |
| L-02 | MENU 기록 | 프론트 `POST /api/menus/access` + 인터셉터 |
| L-03 | API 기록 | `/api/admin/**` 등 업무 API 자동 기록 |

### 제외

- 이력 수정·삭제 UI
- 사용자 단위 필터(후속 가능)

---

## 4. 기록 규칙

| accessType | 트리거 | 비고 |
|------------|--------|------|
| `MENU` | 라우트 변경 후 `/api/menus/access` | request attribute로 menuUrl |
| `API` | 인터셉터 afterCompletion | 메뉴 URL은 URI 정규화·최장 prefix 매칭 |

저장 필드 예: userEmail, menuId/url/name, httpMethod, requestUri, clientIp, userAgent, httpStatus, accessedAt.

기록 실패는 본 요청을 막지 않음(비동기·warn).

---

## 5. API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/admin/menu-access-logs?page&size` | 목록 |
| POST | `/api/menus/access` | 메뉴 진입 마킹(본문 `menuUrl`) |

---

## 6. 데이터

- `pjt_menu_access_logs`

---

## 7. 수용 기준

- [ ] 관리자만 조회
- [ ] 메뉴 이동 시 MENU 이력 생성
- [ ] 관리 API 호출 시 API 이력 생성
- [ ] 목록 페이지네이션

---

## 8. 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 1.0 | 2026-07-21 | 초안(구현 반영) |
