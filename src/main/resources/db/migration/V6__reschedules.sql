-- V6: Reprogramación de Citas (RF-15, RF-18)

CREATE TABLE reschedule_statuses (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT IGNORE INTO reschedule_statuses (id, code, name, is_terminal) VALUES
(1, 'PENDING', 'Pendiente de aprobación', FALSE),
(2, 'APPROVED', 'Aprobada', TRUE),
(3, 'REJECTED', 'Rechazada', TRUE);

CREATE TABLE appointment_reschedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    requested_by_user_id BIGINT NOT NULL,
    old_start_at DATETIME NOT NULL,
    new_start_at DATETIME NOT NULL,
    new_end_at DATETIME NOT NULL,
    status_id SMALLINT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    rejection_reason VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (requested_by_user_id) REFERENCES app_users(id),
    FOREIGN KEY (status_id) REFERENCES reschedule_statuses(id)
);

CREATE INDEX ix_reschedule_appointment ON appointment_reschedules(appointment_id);
CREATE INDEX ix_reschedule_status ON appointment_reschedules(status_id);
