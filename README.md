# miniproject

Vue 3 + Spring Boot 3 기반 풀스택 스타터 프레임워크입니다.

## 프로젝트 구조

```
/workspace/
├── frontend/    # Vue 3 + Vite + JavaScript
└── backend/     # Spring Boot 3 + Maven
```

## 사전 준비

### 1. PostgreSQL

로컬 PostgreSQL(포트 5432)에 `postgres` 데이터베이스가 있어야 합니다.

### 2. 백엔드 DB 설정

[`backend/src/main/resources/application-dev.yml`](backend/src/main/resources/application-dev.yml)에서 PostgreSQL 계정 정보를 수정합니다.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: admin
```

## 실행 방법

### 백엔드 (포트 8080)

```bash
cd backend
./mvnw spring-boot:run
```

### 프론트엔드 (포트 5173)

```bash
cd frontend
npm install
npm run dev
```

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
| GET | `/api/users/me` | 현재 사용자 정보 | 필요 |
| GET | `/api/health` | 헬스체크 | 불필요 |

## Swagger UI

백엔드 실행 후 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)에서 API 문서를 확인할 수 있습니다.
