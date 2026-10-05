-- V4: Recuperación y Restablecimiento de Contraseña (RF-03)
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES app_users(id)
);

CREATE INDEX ix_password_reset_hash ON password_reset_tokens(token_hash);
CREATE INDEX ix_password_reset_user ON password_reset_tokens(user_id);
