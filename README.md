# 예산 관리 어플리케이션

## 📋목차
- [개요](#개요)
- [Skils](#skils)
- [ERD](#erd)
- [프로젝트 설계 및 일정관리](#프로젝트-설계-및-일정관리)
  - [API Reference](#api-reference)
  - [API 구현과정 및 고려사항](#api-구현과정-및-고려사항)
- [Test](#test)
  - [주요 시나리오](#주요-시나리오)

<br/>

---
## 🎯프로젝트 개요

- 개발기간: 2025.09 ~ 2025.12

- 사용자가 월별 예산을 설정하고 지출을 기록하여 재무 목표를 달성할 수 있도록 돕는 개인 재무 관리 애플리케이션입니다.

#### 핵심 가치

- `📊 데이터 기반 예산 추천`: 사용자 히스토리 기반 맞춤형 예산 설계
- `🎯 실시간 지출 가이드`: 현재 지출 패턴 분석 및 오늘의 지출 권장액 제공 
- `🔒 안전한 인증 체계`: Spring Security + JWT 기반 보안 인증 
- `⚡ 고성능 처리`: Redis 캐싱 및 최적화된 쿼리 설계

<br/>

---
## 🛠 기술 스택
언어 및 프레임워크: ![Static Badge](https://img.shields.io/badge/Java-red)
![Static Badge](https://img.shields.io/badge/SpringBoot-grean)
![Static Badge](https://img.shields.io/badge/SpringDataJPA-orange)
<br/>
DB: ![Static Badge](https://img.shields.io/badge/postgreSQL-blue)
![Static Badge](https://img.shields.io/badge/Redis-yellow)
<br/>
ETC: ![Static Badge](https://img.shields.io/badge/SpringSecurity-pink)
![Static Badge](https://img.shields.io/badge/Kafka-skyblue)
<br/>

---
## 🗄 데이터베이스 설계

### ERD
![img_2.png](src/main/resources/static/img/erd.png)

- 주요 테이블
  - users: 사용자 정보 
  - budgets: 카테고리별 월간 예산
  - expenditures: 지출 내역
  - categories: 예산/지출 카테고리

## 📡 API 명세
![img_1.png](src/main/resources/static/img/api.png)
<br/>

## 🏗 시스템 아키텍처
![img.png](src/main/resources/static/img/Architecture.png)

<br/>

## ✨ 주요 기능

<details> 
 
<summary>예산 관리 (Budgets) - click</summary>

#### 예산 관리 (Budgets)

- 카테고리별 월간 예산 설정 및 수정
- 데이터 기반 예산 추천 시스템 
  - 전체 사용자 예산 데이터 기반 카테고리별 비율 계산 
  - 사용자 총 예산 대비 최적 카테고리별 예산 배분 추천 
  - 통계 기반 예산 설계 가이드 제공

</details> 

<details> 

<summary>지출 관리 (Expenditures) - click</summary>

#### 지출 관리 (Expenditures)
- 지출 내역 CRUD 기능
- 고급 필터링 조회 
  - 기간별 (월/일 단위)
  - 카테고리별 
  - 금액 범위 
  - 키워드 검색
- 합계 제외 기능 (통계에서 특정 지출 제외)
- 오늘의 지출 추천 및 가이드
- 자동화된 이메일 알림 시스템 
  - 매일 오전 08:00 - 오늘의 지출 추천 안내 발송 
  - 매일 오후 22:00 - 오늘의 지출 내역 안내 발송 
  - Thymeleaf 템플릿 기반 HTML 이메일

</details> 

<details> 

<summary>사용자 관리 (Users) - click</summary>

#### 사용자 관리 (Users)
- 회원가입 및 로그인
- JWT 기반 인증
- BCrypt 비밀번호 암호화

</details>

<br/>

---
## Test

- 테스트 전략 
  - 단위 테스트: Service/Repository 레벨 
    - `@DataJpaTest`: Repository 계층 
    - `Mockito`: Service 로직 격리 테스트

  - 통합 테스트: API 레벨 
    - `@WebMvcTest`: Controller 계층

- 커버리지 목표 
  - 전체 라인 커버리지: 70% 이상 
  - 핵심 도메인 (Service): 90% 이상

---
## ⚙️ 환경 설정
### 환경 변수 (.env)
```makefile
# 공통 Postgresql 설정
POSTGRES_HOST={POSTGRES_HOST}
POSTGRES_PORT={POSTGRES_PORT}
POSTGRES_DB=${POSTGRES_DB}
POSTGRES_USER=${POSTGRES_USER}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
POSTGRESQL_URL=jdbc:postgresql://{SERVER_PORT}:{POSTGRES_PORT}/{POSTGRES_DB}

# 공통 redis 설정
REDIS_HOST=${REDIS_HOST}
REDIS_PORT={REDIS_PORT}
REDIS_USER=${REDIS_USER}
REDIS_PASSWORD=${REDIS_PASSWORD}

# 공통 kafka 설정
KAFKA_SERVER=${KAFKA_SERVER}
KAFKA_GROUP_ID=${KAFKA_GROUP_ID}

# mail 정보
MAIL_USERNAME=${MAIL_USERNAME}
MAIL_PASSWORD=${MAIL_PASSWORD}

# JWT 설정
JWT_SECRET=${JWT_SECRET}
``` 

### 실행 방법
```bash
# 1. 환경 변수 설정
cp .env.example .env
# .env 파일 수정

# 2. 데이터베이스 마이그레이션
./gradlew flywayMigrate

# 3. 애플리케이션 실행
./gradlew bootRun

# 4. API 문서 확인
# http://localhost:8080/swagger-ui.html
```

---
## 📚 프로젝트 관리
- 일정 관리: [일정관리](https://great-product-fd5.notion.site/b7b4131ed6874ff6825c62499d183230?source=copy_link)
- API 문서: Swagger UI 자동 생성 
- 버전 관리: Git Flow 전략 적용