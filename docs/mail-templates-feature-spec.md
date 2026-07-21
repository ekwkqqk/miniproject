# 메일 템플릿 기능 정의서

| 항목 | 내용 |
|------|------|
| 문서 버전 | 1.0 |
| 작성일 | 2026-07-21 |
| 대상 시스템 | miniproject (Vue 3 + Spring Boot) |
| 상태 | **확정** (구현 기준) |

---

## 1. 목적

발송에 사용할 **메일 템플릿**(제목·본문·수신·활성)을 관리하고, 코드 기준으로 발송(또는 dry-run)한다.

---

## 2. 메뉴·경로

| 항목 | 값 |
|------|-----|
| 메뉴명 | 메일 템플릿 |
| URL | `/admin/mail/templates` |
| i18n key | `menu.mailTemplates` |
| 접근 | `SYSTEM_ADMIN` |

---

## 3. 기능 범위

| ID | 기능 | 설명 |
|----|------|------|
| MT-01 | 목록·상세 | 템플릿 CRUD UI |
| MT-02 | 생성/수정 | code, from/to/cc/bcc, subject, body, html 여부, enabled |
| MT-03 | 삭제 | 템플릿 삭제 |
| MT-04 | 발송 | `templateCode` + params (+ 주소 override) |

---

## 4. 비즈니스 규칙

1. `code` **고유**.
2. 발송 시 템플릿 **enabled** 필수.
3. To(템플릿 또는 override) **1명 이상**.
4. 본문/제목 `{{param}}` 치환.
5. 메일 기능 비활성 설정 시 **실제 발송 없이 로그(dry-run)** 가능.

---

## 5. API

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/admin/mail/templates` | 목록 |
| POST | `/api/admin/mail/templates` | 생성 |
| GET/PUT/DELETE | `/api/admin/mail/templates/{id}` | 상세·수정·삭제 |
| POST | `/api/mail/send` | 템플릿 발송 |

---

## 6. 데이터

- `pjt_mail_templates`

---

## 7. 수용 기준

- [ ] 관리자 CRUD
- [ ] 중복 code 거부
- [ ] 비활성 템플릿 발송 거부
- [ ] 파라미터 치환·dry-run 동작

---

## 8. 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 1.0 | 2026-07-21 | 초안(구현 반영) |
