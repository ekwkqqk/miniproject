# 사용자 Role 관리 기능 정의서

| 항목 | 내용 |
|------|------|
| 문서 버전 | 1.0 |
| 작성일 | 2026-07-21 |
| 대상 시스템 | miniproject (Vue 3 + Spring Boot) |
| 상태 | **확정** (구현 기준) |

---

## 1. 목적

시스템관리자가 **사용자 계정·활성 여부·Role·비밀번호 초기화**를 관리한다.

---

## 2. 메뉴·경로

| 항목 | 값 |
|------|-----|
| 폴더 | 시스템 관리 |
| 메뉴명 | 사용자 Role 관리 |
| URL | `/admin/users` |
| i18n key | `menu.users` |
| 접근 | `SYSTEM_ADMIN` + `/api/admin/**` 필터 |
| 버튼 | 시드: `canRead`, `canUpdate` |

---

## 3. 기능 범위

| ID | 기능 | 설명 |
|----|------|------|
| U-01 | 목록 | 사용자 목록 조회 |
| U-02 | 등록 | 이메일·비밀번호·이름·활성·Role |
| U-03 | 수정 | 이름·활성·Role 변경 |
| U-04 | 활성 토글 | enabled on/off |
| U-05 | Role 할당 | 사용자–Role 매핑 |
| U-06 | 비밀번호 초기화 | 설정값(`app.user.reset-password`)으로 리셋, 실패횟수 초기·세션 폐기 |

### 제외

- 사용자 물리 삭제 API
- 본인 계정 Role/활성 변경 (금지)

---

## 4. 비즈니스 규칙

1. 이메일 고유.
2. 비밀번호 최소 길이는 **시스템 설정** 값 적용.
3. 신규 사용자 기본 Role은 설정의 **default role codes**.
4. **본인**의 활성·Role은 변경 불가.
5. 비활성화 시 refresh 토큰 폐기.
6. 비밀번호 초기화 후 로그인 실패 카운터 초기·세션 폐기.

---

## 5. API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/admin/users` | 목록 |
| POST | `/api/admin/users` | 생성 |
| PUT | `/api/admin/users/{userId}` | 수정 |
| PUT | `/api/admin/users/{userId}/roles` | Role 할당 |
| PUT | `/api/admin/users/{userId}/enabled` | 활성 |
| POST | `/api/admin/users/{userId}/reset-password` | 비밀번호 초기화 |
| GET | `/api/admin/roles` | Role 선택용 |

---

## 6. 데이터

- `pjt_users`, `pjt_user_roles`, `pjt_roles`, `pjt_refresh_tokens`

---

## 7. 수용 기준

- [ ] 관리자만 화면·API 접근
- [ ] 생성·수정·Role·활성·초기화 동작
- [ ] 본인 Role/활성 변경 거부
- [ ] 비밀번호 길이·기본 Role이 설정과 연동

---

## 8. 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 1.0 | 2026-07-21 | 초안(구현 반영) |
