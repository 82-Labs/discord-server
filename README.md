# Discord Server

Discord 서버 기능을 제공하는 Spring Boot 멀티 모듈 프로젝트

## 모듈 구조

- **common**: 공통 유틸리티와 기본 설정
- **domain**: 순수 비즈니스 도메인 로직
- **discord-app**: 메인 애플리케이션 (웹, DB, 보안 설정)


## 환경 설정

환경 변수는 `discord-app/env/local.env` 파일에 설정합니다.

```bash
# 환경 변수 파일 복사
cp discord-app/env/sample.env discord-app/env/local.env
```

## 실행 방법

```bash
# 전체 서비스 (애플리케이션 + MySQL + Redis) 실행
docker-compose up --build
```

## 접속 정보

- **애플리케이션**: http://localhost:8080
- **MySQL**: localhost:3306
- **MongoDB**: localhost:27017
- **Redis**: localhost:6379