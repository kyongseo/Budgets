
# 예산 관리 어플리케이션

<br/>

## 프로젝트 기간
2025.08. ~ 2025

<br/>

## Table of Contents
- [개요](#개요)
- [Skils](skils)
- [ERD](#erd)
- [프로젝트 설계 및 일정관리](#프로젝트-설계-및-일정관리)
- [API Reference](#api-reference)
- [API 구현과정 및 고려사항](#api-구현과정-및-고려사항)
- [Test](#test)

<br/>

## 개요
본 서비스는 사용자들이 개인 재무를 관리하고 지출을 추적하는 데 도움을 주는 애플리케이션입니다. 이 앱은 사용자들이 예산을 설정하고 지출을 모니터링하며 재무 목표를 달성하는 데 도움이 됩니다.

<br/>

## Skils
언어 및 프레임워크: ![Static Badge](https://img.shields.io/badge/Java-red)
![Static Badge](https://img.shields.io/badge/SpringBoot-grean)
![Static Badge](https://img.shields.io/badge/SpringDataJPA-grean)
<br/>
데이터 베이스: ![Static Badge](https://img.shields.io/badge/postgreSQL-blue)

<br/>

## ERD

<br/>

## 프로젝트 설계 및 일정관리


<br/>

## API Reference


<br/>

## API 구현과정 및 고려사항
<details>
<summary>Budgets - click</summary>

- Budget 카테고리 목록 조회
    - [GET] /api/budget-categories
    
- Budget 설정
    - [POST] /api/budgets
    
- Budget 수정
    - [PATCH] /api/budgets/{budgetId}
    
- Budget 설계 추천
    - [GET] /api/budgets/recommend
    
</details>

<details>
<summary>Expenditures - click</summary>

- Expenditure 생성
    - [POST] /api/expenditures

- Expenditure 수정
    - [PATCH] /api/expenditures/{expenditureId}

- Expenditure 목록 조회
    - [GET] /api/expenditures

- Expenditure 상세 조회
    - [GET] /api/expenditures/{expenditureId}

- Expenditure 삭제
    - [DELETE] /api/expenditures/{expenditureId}
    
- Expenditure 합계 제외
    - [PATCH] /api/expenditures/except/{expenditureId}
    
- Expenditure 추천
    - [GET] /api/expenditure/recommend
    
- Expenditure 안내
    - [GET] /api/expenditure/guide
    
</details>

<details>
<summary>Users - click</summary>

- User 회원가입
    - [POST] /api/users
    
- User 로그인
    - [POST] /api/users/login
    
</details>
<br/>

## Test
