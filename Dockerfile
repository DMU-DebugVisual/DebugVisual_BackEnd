FROM openjdk:17-slim

# Docker 명령어 설치 (컨테이너 내에서 도커 실행 필요 시)
RUN apt-get update && apt-get install -y docker.io

# 작업 디렉토리 생성
WORKDIR /app

# 백엔드 JAR 복사
COPY build/libs/Debug_Visual-0.0.1-SNAPSHOT.jar app.jar

# Python/C/Java entrypoint.sh 스크립트들 복사
COPY docker/python/entrypoint.sh /docker/python/entrypoint.sh
COPY docker/java/entrypoint.sh /docker/java/entrypoint.sh
COPY docker/c/entrypoint.sh /docker/c/entrypoint.sh

# 실행 권한 부여
RUN chmod +x /docker/python/entrypoint.sh \
    && chmod +x /docker/java/entrypoint.sh \
    && chmod +x /docker/c/entrypoint.sh

# 앱 실행
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
