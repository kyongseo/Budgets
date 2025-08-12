# Build stage

# Base 이미지 설정 (Alpine 기반 OpenJDK 17 사용)
FROM bellsoft/liberica-openjdk-alpine:17

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

## 컨테이너가 시작될 때 실행할 명령어 설정
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]