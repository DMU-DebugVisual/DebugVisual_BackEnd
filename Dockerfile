# OpenJDK 17 기반의 경량 이미지 사용
FROM openjdk:17-jdk-slim

# 작업 디렉토리 생성
WORKDIR /app

# Gradle 빌드된 JAR 파일 복사
COPY build/libs/Debug_Visual-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 실행
CMD ["java", "-jar", "app.jar"]

# 애플리케이션이 실행될 포트 지정
EXPOSE 8080
