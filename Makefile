# JAR 빌드
service-build:
	./gradlew build -x test

# 도커 빌드 및 실행
service-run: build
	docker-compose up --build -d

# 도커 컨테이너 재시작
service-restart: build
	docker restart mongo-db
	docker restart postgresql-db
	docker restart redis-db

# 도커 컨테이너 정리
service-clean:
	docker-compose down