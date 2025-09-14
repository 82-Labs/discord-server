-- V2__Create_user_relations_tables.sql
-- 사용자 관계 관련 테이블 생성

-- 사용자 관계 테이블 (친구, 차단 등)
CREATE TABLE user_relation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '관계를 맺는 사용자 ID',
    related_user_id BIGINT NOT NULL COMMENT '관계 대상 사용자 ID',
    relation_type VARCHAR(20) NOT NULL COMMENT '관계 유형 (FRIEND, BLOCKED)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '관계 생성일시',
    
    PRIMARY KEY (id)
);

-- 사용자별 관계 조회 인덱스
CREATE INDEX idx_user_relation_user_id ON user_relation(user_id);
CREATE INDEX idx_user_relation_related_user_id ON user_relation(related_user_id);

-- 관계 유형별 조회 인덱스
CREATE INDEX idx_user_relations_user_type ON user_relations(user_id, relation_type);

-- 중복 관계 방지 유니크 인덱스
CREATE UNIQUE INDEX idx_user_relations_unique ON user_relations(user_id, related_user_id);

-- 사용자 관계 요청 테이블 (친구 요청 등)
CREATE TABLE user_relation_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sender_id BIGINT NOT NULL COMMENT '요청 보낸 사용자 ID',
    receiver_id BIGINT NOT NULL COMMENT '요청 받은 사용자 ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '요청 상태 (PENDING, ACCEPTED, REJECTED, CANCELED)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '요청 생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '상태 변경일시',
    
    PRIMARY KEY (id)
);

-- 발신자별 요청 조회 인덱스
CREATE INDEX idx_relation_request_sender ON user_relation_request(sender_id);

-- 수신자별 대기중인 요청 조회 인덱스
CREATE INDEX idx_relation_request_receiver_status ON user_relation_request(receiver_id, status);

-- 중복 요청 방지를 위한 복합 인덱스 (PENDING 상태의 중복 요청 방지)
CREATE INDEX idx_relation_request_unique_pending ON user_relation_request(sender_id, receiver_id, status);