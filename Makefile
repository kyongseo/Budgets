.PHONY: service-build service-run service-restart service-clean swagger-generate swagger-deploy swagger-update

# JAR 빌드
service-build:
	./gradlew build -x test

# 앱 실행
service-boot-run:
	./gradlew bootRun

# 도커 빌드 및 실행
service-run: service-build
	docker-compose up --build -d

# 도커 컨테이너 재시작
service-restart:
	docker restart zookeeper
	docker restart kafka
	docker restart postgresql-db
	docker restart redis-db

# 도커 컨테이너 정리
service-clean:
	docker-compose down

# OpenAPI JSON 생성 (애플리케이션이 실행 중이어야 함)
swagger-generate:
	@echo "🚀 Generating OpenAPI JSON..."
	@curl http://localhost:9091/v3/api-docs -o swagger-ui/openapi.json
	@echo "✅ OpenAPI JSON generated at swagger-ui/openapi.json"

# Swagger UI를 Git에 커밋하고 배포
swagger-deploy: swagger-generate
	@echo "📦 Deploying Swagger UI to GitHub Pages..."
	@git add swagger-ui/
	@git commit -m "Update Swagger API documentation"
	@git push origin main
	@echo "✅ Swagger UI deployed!"

# 전체 프로세스: Docker 실행 → 앱 시작 → JSON 생성
swagger-update: service-run
	@echo "⏳ Waiting for application to start..."
	@sleep 30
	@$(MAKE) swagger-generate
	@echo "✅ Swagger docs updated!"