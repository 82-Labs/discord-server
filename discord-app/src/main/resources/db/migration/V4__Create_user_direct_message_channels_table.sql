CREATE TABLE user_direct_message_channels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    is_hidden BOOLEAN DEFAULT FALSE,
    
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_user_channel (user_id, channel_id)
);