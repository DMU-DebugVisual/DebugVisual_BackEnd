FROM openjdk:17-slim

# docker 명령어 설치
RUN apt-get update && apt-get install -y docker.io

WORKDIR /app
COPY build/libs/Debug_Visual-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
