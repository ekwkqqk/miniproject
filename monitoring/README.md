# Monitoring (Prometheus + Grafana)

로컬에서 Spring Boot 메트릭을 Prometheus로 수집하고 Grafana 대시보드로 시각화합니다.

## 구성

| 서비스 | URL | 계정 |
|--------|-----|------|
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | `admin` / `admin` |
| App metrics | http://localhost:8080/actuator/prometheus | (공개, 스크레이프용) |

## 사전 조건

1. 백엔드가 **8080** 포트에서 실행 중이어야 합니다.
2. Docker Desktop(또는 Docker Engine)이 필요합니다.

## 실행

```bash
# 1) 백엔드 기동
cd backend
./mvnw spring-boot:run

# 2) 모니터링 스택 기동 (repo root)
docker compose -f monitoring/docker-compose.yml up -d
```

중지:

```bash
docker compose -f monitoring/docker-compose.yml down
```

데이터 볼륨까지 삭제:

```bash
docker compose -f monitoring/docker-compose.yml down -v
```

## Grafana 대시보드

프로비저닝으로 **MiniProject / MiniProject Spring Boot Overview** 대시보드가 자동 등록됩니다.

주요 패널:
- UP / CPU / Heap / RPS / 5xx 비율 / 스레드
- HTTP URI별 요청량·p95 지연
- JVM 메모리·스레드
- HikariCP 커넥션
- GC pause

## 디렉터리

```
monitoring/
├── docker-compose.yml
├── prometheus/prometheus.yml
└── grafana/
    ├── dashboards/spring-boot-overview.json
    └── provisioning/
        ├── datasources/datasource.yml
        └── dashboards/dashboard.yml
```

## 보안 참고

- `/actuator/prometheus` 는 스크레이프를 위해 인증 없이 열려 있습니다.
- 운영에서는 사설망/방화벽으로 Actuator를 격리하거나, reverse proxy에서 IP 제한을 적용하세요.
