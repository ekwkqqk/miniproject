# miniproject

Vue 3 + Spring Boot 4 기반 풀스택 스타터 프레임워크입니다.  
인증·권한·메뉴·다국어·메일·파일·테마(다크 모드) 등 관리형 웹앱에 바로 쓸 수 있는 공통 기능을 포함합니다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Frontend | Vue 3, Vite, Pinia, Vue Router, Element Plus, Axios |
| Backend | Spring Boot 4.1, Spring Security, JWT, MyBatis, Liquibase, springdoc-openapi, Jackson 3 |
| DB | PostgreSQL |
| Java / Node | Java 17, Node `^22.18` 또는 `>=24.12` |

## 프로젝트 구조

```
miniproject/
├── frontend/                 # Vue 3 + Vite + JavaScript
│   └── src/
│       ├── features/         # 도메인별 기능 (auth, admin, menu, i18n, …)
│       ├── shared/           # 레이아웃, 공통 스타일, 유틸
│       └── router/           # 라우터·가드
└── backend/                  # Spring Boot 4 + MyBatis + Liquibase + Maven
    └── src/main/
        ├── java/com/miniproject/
        └── resources/
            ├── db/changelog/ # Liquibase 마이그레이션
            └── mapper/       # MyBatis XML
```

## 주요 기능

- **인증**: 회원가입/로그인, JWT access + refresh(쿠키), 로그아웃, 비밀번호 변경·만료 처리, 로그인 실패 잠금
- **권한**: 역할(`SYSTEM_ADMIN` 등) 기반 API·메뉴 접근 제어
- **사용자/역할 관리**: 관리자 화면에서 사용자·역할 CRUD, 역할별 사용자 조회
- **메뉴**: 계층 메뉴, 역할 매핑, 사용 여부(`enabled`), 사이드바·접근 로그
- **다국어(i18n)**: locale / group / message 관리, 메시지 페이징, 프론트 `tCode` 조회
- **설정·테마**: 공개/관리 설정, primary 색상, **다크 모드**(localStorage 유지)
- **메일**: 템플릿 관리, 발송 API (`MAIL_ENABLED`로 활성화)
- **파일**: 업로드/다운로드/삭제, file group
- **데모 화면**: 검색·조회·편집·업로드·팝업 등 UI 샘플

## 사전 준비

### 1. PostgreSQL

로컬 PostgreSQL(포트 5432)에 `postgres` 데이터베이스가 있어야 합니다.

### 2. 백엔드 DB 설정

[`backend/src/main/resources/application-dev.yml`](backend/src/main/resources/application-dev.yml)에서 계정 정보를 수정합니다.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: admin
```

### 3. 스키마 (Liquibase)

- 변경 이력: `backend/src/main/resources/db/changelog/`
- 활성화: `spring.liquibase.enabled` (기본 `true`, 환경변수 `LIQUIBASE_ENABLED`)
- 테이블 prefix: `pjt_` (예: `pjt_users`, `pjt_roles`, `pjt_menus`)
- 영속화: MyBatis (`backend/src/main/resources/mapper/**/*.xml`)
- 예전 JPA/`ddl-auto`로 만든 prefix 없는 테이블이 남아 있으면 `pjt_*`와 별개입니다. 로컬에서는 구 테이블 삭제 또는 DB 초기화 후 재시작을 권장합니다.

### 4. 최초 시스템 계정

앱 기동 시 아래 계정이 없으면 자동 생성됩니다.

| 항목 | 값 |
|------|-----|
| 이메일 | `admin@system.local` |
| 비밀번호 | `Admin123!` (환경변수 `SYSTEM_ADMIN_PASSWORD`로 변경 가능) |
| Role | `SYSTEM_ADMIN` |

## 실행 방법

### 백엔드 (포트 8080)

```bash
cd backend
./mvnw spring-boot:run
```

Windows:

```bash
cd backend
mvnw.cmd spring-boot:run
```

### 프론트엔드 (포트 5173)

```bash
cd frontend
npm install
npm run dev
```

브라우저: [http://localhost:5173](http://localhost:5173)

### 프로덕션 빌드 (프론트)

```bash
cd frontend
npm run build
npm run preview
```

## 환경 변수 (주요)

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `JWT_SECRET` | JWT 서명 키 (32자 이상 권장) | 개발용 플레이스홀더 |
| `SYSTEM_ADMIN_PASSWORD` | 시드 관리자 비밀번호 | `Admin123!` |
| `LIQUIBASE_ENABLED` | Liquibase 실행 여부 | `true` |
| `MAIL_ENABLED` | 메일 발송 활성화 | `false` |
| `FILE_UPLOAD_DIR` | 업로드 저장 경로 | `./uploads` |
| `FILE_MAX_SIZE` | 단일 파일 최대 크기 | `20MB` |
| `FILE_MAX_REQUEST_SIZE` | 요청 최대 크기 | `50MB` |

Access JWT 만료: 15분(`900000` ms), Refresh: 7일(`604800000` ms) — `application.yml`의 `app.jwt.*`에서 조정합니다.

## API 규약

모든 API 응답은 아래 형식을 따릅니다.

```json
{
  "success": true,
  "data": {},
  "message": "성공 메시지",
  "errorCode": null
}
```

## 주요 API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/auth/register` | 회원가입 | 불필요 |
| POST | `/api/auth/login` | 로그인 (JWT 발급) | 불필요 |
| POST | `/api/auth/refresh` | access 토큰 갱신 | refresh 쿠키 |
| POST | `/api/auth/logout` | 로그아웃 | 필요 |
| POST | `/api/auth/change-password` | 비밀번호 변경 | 상황에 따라 |
| GET | `/api/users/me` | 현재 사용자 | 필요 |
| GET | `/api/menus/my` | 내 메뉴 트리(사용 중만) | 필요 |
| GET | `/api/i18n/locales` | 공개 locale 목록 | 불필요 |
| GET | `/api/i18n/messages` | 공개 메시지 | 불필요 |
| GET | `/api/settings/public` | 공개 설정(테마 등) | 불필요 |
| GET | `/api/health` | 헬스체크 | 불필요 |
| * | `/api/admin/**` | 관리자 API (사용자·역할·메뉴·i18n·메일·설정 등) | `SYSTEM_ADMIN` |
| * | `/api/files/**` | 파일 업로드/다운로드 | 필요 |

전체 목록은 Swagger UI를 참고하세요.

## Swagger UI

백엔드 실행 후 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 프론트엔드 메모

- 기능 단위는 `frontend/src/features/*`에 두고, 라우트·API·화면을 같은 폴더에 모읍니다.
- Access 토큰 만료 시 refresh로 갱신하고, 둘 다 만료되면 로그인으로 이동합니다 (`?redirect=` 지원).
- 다크 모드: 헤더(또는 로그인 화면) 토글, `localStorage` 키 `app.darkMode`.
- 테마 primary 색: 설정/로컬 저장 후 Element Plus·사이드바 CSS 변수에 반영됩니다.
- 개발 프로필에서 MyBatis SQL은 mapper 패키지 `DEBUG` 로그로 출력됩니다.

## 라이선스

프로젝트 정책에 따릅니다.
