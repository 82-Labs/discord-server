-- V1__Create_users_table.sql
-- 사용자 및 인증 관련 테이블 생성

-- 사용자 테이블
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nickname VARCHAR(100) NOT NULL COMMENT '사용자 닉네임 (2~30자)',
    username VARCHAR(50) NOT NULL COMMENT '사용자명 (2~30자, 영문/숫자/언더스코어만)',
    roles VARCHAR(200) NOT NULL DEFAULT 'USER' COMMENT '사용자 역할들 (USER,ADMIN,TEMPORAL 등 콤마로 구분)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    PRIMARY KEY (id)
);

-- 사용자명 유니크 인덱스 (중복 체크용)
CREATE UNIQUE INDEX idx_users_username ON users(username);

-- 인증 자격증명 테이블 (소셜 로그인 정보)
CREATE TABLE auth_credential (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL COMMENT '연결된 사용자 ID (NULL일 경우 임시 사용자)',
    provider VARCHAR(50) NOT NULL COMMENT '소셜 로그인 제공자 (KAKAO, GOOGLE, NAVER 등)',
    external_id VARCHAR(255) NOT NULL COMMENT '외부 시스템의 고유 식별자',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    PRIMARY KEY (id)
);

-- 외부ID + 제공자 유니크 인덱스 (소셜 로그인 중복 체크용)
CREATE UNIQUE INDEX idx_auth_credentials_external_id_provider ON auth_credential(external_id, provider);

-- user_id 인덱스 (조회 성능 향상)
CREATE INDEX idx_auth_credentials_user_id ON auth_credential(user_id);