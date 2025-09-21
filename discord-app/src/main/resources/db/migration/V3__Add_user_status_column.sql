-- V3__Add_user_status_column.sql
-- 사용자 테이블에 상태 컬럼 추가

ALTER TABLE users 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'NONE' 
COMMENT '사용자 상태 (NONE, ONLINE, IDLE, DO_NOT_DISTURB, INVISIBLE, OFFLINE)'
AFTER roles;