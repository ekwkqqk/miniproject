# 다국어 관리 기능 정의서

| 항목 | 내용 |
|------|------|
| 문서 버전 | 1.0 |
| 작성일 | 2026-07-21 |
| 대상 시스템 | miniproject (Vue 3 + Spring Boot) |
| 상태 | **확정** (구현 기준) |

---

## 1. 목적

UI·메뉴명 등에 쓰는 **로케일·메시지 그룹·메시지(로케일별 텍스트)** 를 관리하고, 런타임에 번들을 제공한다.

---

## 2. 메뉴·경로

| 메뉴명 | URL | i18n key |
|--------|-----|----------|
| (폴더) 다국어 관리 | — | `menu.i18n` |
| 로케일 관리 | `/admin/i18n/locales` | `menu.locales` |
| 메시지 그룹 | `/admin/i18n/groups` | `menu.groups` |
| 메시지 관리 | `/admin/i18n/messages` | `menu.messages` |

접근: `SYSTEM_ADMIN` + `/api/admin/**`.

---

## 3. 기능 범위

### 3.1 로케일

| ID | 기능 | 설명 |
|----|------|------|
| I-01 | CRUD | code 고유, 활성·정렬 |

### 3.2 메시지 그룹

| ID | 기능 | 설명 |
|----|------|------|
| I-02 | CRUD | code 고유 |
| I-03 | 삭제 연쇄 | 그룹 삭제 시 메시지·텍스트 제거 |

### 3.3 메시지

| ID | 기능 | 설명 |
|----|------|------|
| I-04 | CRUD·페이지 | `(group, code)` 고유 |
| I-05 | 로케일별 텍스트 | locale → text 맵 |

### 3.4 런타임

| ID | 기능 | 설명 |
|----|------|------|
| I-10 | 로케일 목록 | 공개 GET |
| I-11 | 메시지 번들 | 공개 GET (앱 부트스트랩) |
| I-12 | resolve | 파라미터 치환(`MessageFormatter`) |

---

## 4. 비즈니스 규칙

1. 메시지: `(group_id, code)` 유일.
2. 텍스트: `(message_id, locale_id)` 유일.
3. 메뉴 `nameI18nKey`와 시드 메시지로 메뉴명 다국어 연동.
4. 관리 API는 시스템관리자 전용. 런타임 GET은 permitAll(또는 인증 정책에 따름).

---

## 5. API

| Method | Path | 설명 |
|--------|------|------|
| * | `/api/admin/i18n/locales` | 로케일 CRUD |
| * | `/api/admin/i18n/groups` | 그룹 CRUD |
| * | `/api/admin/i18n/messages` (+ `/page`) | 메시지 CRUD·목록 |
| GET | `/api/i18n/locales` | 런타임 로케일 |
| GET | `/api/i18n/messages` | 런타임 번들 |
| POST | `/api/i18n/resolve` | 파라미터 해석 |

---

## 6. 데이터

- `pjt_i18n_locales`
- `pjt_i18n_message_groups`
- `pjt_i18n_messages`
- `pjt_i18n_message_texts`

---

## 7. 수용 기준

- [ ] 로케일·그룹·메시지 CRUD
- [ ] 그룹 삭제 시 하위 정리
- [ ] 프론트 로케일 전환 시 메뉴·공통 문구 반영
- [ ] 비관리자 관리 API 403

---

## 8. 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 1.0 | 2026-07-21 | 초안(구현 반영) |
