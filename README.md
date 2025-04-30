# Debug Visual Backend
https://respected-jitterbug-a9b.notion.site/Commit-Issue-PR-1bcdd1e1f08280db8de2c766a5892210?pvs=4

## 프로젝트 설명

Spring Boot 백엔드와 MySQL 데이터베이스를 Docker 환경에서 실행합니다.  
Docker와 Docker Compose가 설치되어 있어야 합니다.

---

## 필수 설치 프리즈어미

- [Docker Desktop](https://www.docker.com/products/docker-desktop)
- (옵션) IntelliJ / VS Code 등의 IDE

---

## 실행 방법

### 1. 프로젝트 클론

```bash
git clone https://github.com/DMU-DebugVisual/DebugVisual_BackEnd.git
cd DebugVisual_BackEnd
```

### 2. Gradle 빌드

```bash
./gradlew clean build
```

### 3. Docker Compose 실행

```bash
docker compose up --build
```

> 처음 실행 시는 이미지 생성과 빌드 때문에 조금 시간이 걸리며, Swagger UI 설정이 되어 있다면 `http://localhost:8080/swagger-ui/index.html` 로 접속 가능합니다.

---

## application.properties 파일 설정

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### docker-compose.yml에서 해당 값은 자동 주입됩니다:

```yaml
backend:
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/debugdb
    SPRING_DATASOURCE_USERNAME: root
    SPRING_DATASOURCE_PASSWORD: [본인 mysql root 비밀번호]
```

---

## 정상 실행 확인

- 백엔드 실행: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- DB 포트: 3306 (MySQL 9.0)

---

## 종료 방법

```bash
docker compose down
```

---

## 문제 해결

| 문제 | 해결 방법 |
|--------|--------------------|
| 3306 포트 충돌 | `brew services stop mysql` 통해 로컬 MySQL 종료 |
| 빌드 실패 | `./gradlew clean build` 재실행 후 `docker compose up --build` |
| DB 연결 오류 | docker-compose.yml 내 DB 설정 확인 |

---

## 문의

문제 발생 시 GitHub Issue로 공유해주세요! 🙋‍♀️

