-- V2: Sedes, Especialidades, Profesionales, Bloques de Disponibilidad, Slots, Citas e Historial

CREATE TABLE locations (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_locations_code UNIQUE (code)
);

CREATE TABLE specialties (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    appointment_duration_minutes SMALLINT NOT NULL DEFAULT 30,
    is_general BOOLEAN NOT NULL DEFAULT FALSE,
    requires_admin_approval BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_specialties_code UNIQUE (code)
);

CREATE TABLE appointment_statuses (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(50) NOT NULL,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_appointment_statuses_code UNIQUE (code)
);

CREATE TABLE professionals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    professional_code VARCHAR(50) NOT NULL,
    license_number VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_professionals_user UNIQUE (user_id),
    CONSTRAINT uk_professionals_code UNIQUE (professional_code),
    CONSTRAINT uk_professionals_license UNIQUE (license_number),
    FOREIGN KEY (user_id) REFERENCES app_users(id)
);

CREATE TABLE professional_specialties (
    professional_id BIGINT NOT NULL,
    specialty_id SMALLINT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (professional_id, specialty_id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id),
    FOREIGN KEY (specialty_id) REFERENCES specialties(id)
);

CREATE TABLE professional_locations (
    professional_id BIGINT NOT NULL,
    location_id SMALLINT NOT NULL,
    PRIMARY KEY (professional_id, location_id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE availability_blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT NOT NULL,
    location_id SMALLINT NOT NULL,
    available_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (professional_id) REFERENCES professionals(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_user_id BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    location_id SMALLINT NOT NULL,
    specialty_id SMALLINT NOT NULL,
    status_id SMALLINT NOT NULL,
    scheduled_start_at DATETIME NOT NULL,
    scheduled_end_at DATETIME NOT NULL,
    rejection_reason VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_user_id) REFERENCES app_users(id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id),
    FOREIGN KEY (location_id) REFERENCES locations(id),
    FOREIGN KEY (specialty_id) REFERENCES specialties(id),
    FOREIGN KEY (status_id) REFERENCES appointment_statuses(id)
);

CREATE TABLE professional_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    availability_block_id BIGINT NOT NULL,
    appointment_id BIGINT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    FOREIGN KEY (availability_block_id) REFERENCES availability_blocks(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

CREATE INDEX ix_slots_block ON professional_slots(availability_block_id);
CREATE INDEX ix_slots_range ON professional_slots(start_at, end_at);
CREATE INDEX ix_slots_appointment ON professional_slots(appointment_id);

CREATE TABLE appointment_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    status_id SMALLINT NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    change_source VARCHAR(20) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(500) NULL,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (status_id) REFERENCES appointment_statuses(id),
    FOREIGN KEY (changed_by_user_id) REFERENCES app_users(id)
);

-- ========================================================
-- Semillas de Catálogos Fijos
-- ========================================================
INSERT IGNORE INTO locations (id, code, name, address, active) VALUES
(1, 'HIC', 'Hospital Internacional de Colombia', 'Km 7 Autopista Bucaramanga–Piedecuesta, Valle de Menzulí, Santander', TRUE),
(2, 'ICV', 'Fundación Cardiovascular de Colombia / Instituto Cardiovascular', 'Calle 155A No. 23-58, Urbanización El Bosque, Floridablanca, Santander', TRUE);

INSERT IGNORE INTO specialties (id, code, name, appointment_duration_minutes, is_general, requires_admin_approval, active) VALUES
(1, 'MED_GENERAL', 'Medicina General', 30, TRUE, FALSE, TRUE),
(2, 'CARDIOLOGIA', 'Cardiología', 60, FALSE, TRUE, TRUE),
(3, 'PEDIATRIA', 'Pediatría', 30, FALSE, TRUE, TRUE),
(4, 'DERMATOLOGIA', 'Dermatología', 30, FALSE, TRUE, TRUE),
(5, 'ORTOPEDIA', 'Ortopedia', 30, FALSE, TRUE, TRUE);

INSERT IGNORE INTO appointment_statuses (id, code, name, is_terminal) VALUES
(1, 'REQUESTED', 'Solicitada', FALSE),
(2, 'APPROVED', 'Aprobada', FALSE),
(3, 'REJECTED', 'Rechazada', TRUE),
(4, 'CANCELLED', 'Cancelada', TRUE),
(5, 'COMPLETED', 'Completada', TRUE),
(6, 'NO_SHOW', 'No asistió', TRUE);

-- ========================================================
-- Semillas de Usuarios Sintéticos para Pruebas del Laboratorio
-- ========================================================
-- Admin: admin@fcv.test / Admin123*
INSERT IGNORE INTO app_users (id, first_name, last_name, document_type, document_number, email, phone, password_hash, active) VALUES
(100, 'Administrador', 'FCV Citas', 'CC', '1000000001', 'admin@fcv.test', '+57 300 111 2233', '$2a$12$.jb7v/U.mmm8MaCrsO0UVehjq69n2ZeLAzPE4xJZzNYEAbccp43Om', TRUE);
INSERT IGNORE INTO user_roles (user_id, role_code) VALUES (100, 'USER'), (100, 'ADMIN');

-- Profesional 1: Dr. Carlos Mendoza (Medicina General) / Doc123*
INSERT IGNORE INTO app_users (id, first_name, last_name, document_type, document_number, email, phone, password_hash, active) VALUES
(101, 'Carlos', 'Mendoza', 'CC', '1000000002', 'dr.mendoza@fcv.test', '+57 300 222 3344', '$2a$12$NeqZ94gn4v9cbBhNN.QxfeG.Lk9B7KtvoFrjXt2pj//qIQGslRk5u', TRUE);
INSERT IGNORE INTO user_roles (user_id, role_code) VALUES (101, 'USER'), (101, 'PROFESSIONAL');

-- Profesional 2: Dra. Sofía Castro (Cardiología) / Doc123*
INSERT IGNORE INTO app_users (id, first_name, last_name, document_type, document_number, email, phone, password_hash, active) VALUES
(102, 'Sofía', 'Castro', 'CC', '1000000003', 'dra.castro@fcv.test', '+57 300 333 4455', '$2a$12$NeqZ94gn4v9cbBhNN.QxfeG.Lk9B7KtvoFrjXt2pj//qIQGslRk5u', TRUE);
INSERT IGNORE INTO user_roles (user_id, role_code) VALUES (102, 'USER'), (102, 'PROFESSIONAL');

-- Profesional 3: Dr. Andrés Ruiz (Pediatría) / Doc123*
INSERT IGNORE INTO app_users (id, first_name, last_name, document_type, document_number, email, phone, password_hash, active) VALUES
(103, 'Andrés', 'Ruiz', 'CC', '1000000004', 'dr.ruiz@fcv.test', '+57 300 444 5566', '$2a$12$NeqZ94gn4v9cbBhNN.QxfeG.Lk9B7KtvoFrjXt2pj//qIQGslRk5u', TRUE);
INSERT IGNORE INTO user_roles (user_id, role_code) VALUES (103, 'USER'), (103, 'PROFESSIONAL');

-- Paciente sintético: Laura Martínez / User123*
INSERT IGNORE INTO app_users (id, first_name, last_name, document_type, document_number, email, phone, password_hash, active) VALUES
(104, 'Laura', 'Martínez', 'CC', '1000000005', 'paciente@fcv.test', '+57 300 555 6677', '$2a$12$cXROFTFaEqh/uogkkHMm5e2MbT7CNZlHHwG1/s1yk9sn14qA08cmG', TRUE);
INSERT IGNORE INTO user_roles (user_id, role_code) VALUES (104, 'USER');

-- ========================================================
-- Perfiles Profesionales
-- ========================================================
INSERT IGNORE INTO professionals (id, user_id, professional_code, license_number, active) VALUES
(1, 101, 'PROF-GEN-001', 'MP-12345-COL', TRUE),
(2, 102, 'PROF-CARDIO-002', 'MP-67890-COL', TRUE),
(3, 103, 'PROF-PED-003', 'MP-11223-COL', TRUE);

-- Especialidades
INSERT IGNORE INTO professional_specialties (professional_id, specialty_id, is_primary) VALUES
(1, 1, TRUE),  -- Dr. Mendoza: Medicina General (Primaria)
(2, 2, TRUE),  -- Dra. Castro: Cardiología (Primaria)
(3, 3, TRUE);  -- Dr. Ruiz: Pediatría (Primaria)

-- Sedes
INSERT IGNORE INTO professional_locations (professional_id, location_id) VALUES
(1, 1), (1, 2), -- Dr. Mendoza atiende en HIC e ICV
(2, 1),         -- Dra. Castro atiende en HIC
(3, 2);         -- Dr. Ruiz atiende en ICV

-- ========================================================
-- Bloques de Disponibilidad y Slots Sintéticos Iniciales
-- (Día 1: 2026-10-01 y Día 2: 2026-10-02)
-- ========================================================
-- Bloque 1: Dr. Mendoza en HIC, 2026-10-01 08:00 a 11:00 (6 slots de 30 min)
INSERT IGNORE INTO availability_blocks (id, professional_id, location_id, available_date, start_time, end_time, active) VALUES
(1, 1, 1, '2026-10-01', '08:00:00', '11:00:00', TRUE);

INSERT IGNORE INTO professional_slots (availability_block_id, appointment_id, start_at, end_at) VALUES
(1, NULL, '2026-10-01 08:00:00', '2026-10-01 08:30:00'),
(1, NULL, '2026-10-01 08:30:00', '2026-10-01 09:00:00'),
(1, NULL, '2026-10-01 09:00:00', '2026-10-01 09:30:00'),
(1, NULL, '2026-10-01 09:30:00', '2026-10-01 10:00:00'),
(1, NULL, '2026-10-01 10:00:00', '2026-10-01 10:30:00'),
(1, NULL, '2026-10-01 10:30:00', '2026-10-01 11:00:00');

-- Bloque 2: Dra. Castro en HIC, 2026-10-01 14:00 a 17:00 (Cardiología - slots 30 min, requiere pares para 60 min)
INSERT IGNORE INTO availability_blocks (id, professional_id, location_id, available_date, start_time, end_time, active) VALUES
(2, 2, 1, '2026-10-01', '14:00:00', '17:00:00', TRUE);

INSERT IGNORE INTO professional_slots (availability_block_id, appointment_id, start_at, end_at) VALUES
(2, NULL, '2026-10-01 14:00:00', '2026-10-01 14:30:00'),
(2, NULL, '2026-10-01 14:30:00', '2026-10-01 15:00:00'),
(2, NULL, '2026-10-01 15:00:00', '2026-10-01 15:30:00'),
(2, NULL, '2026-10-01 15:30:00', '2026-10-01 16:00:00'),
(2, NULL, '2026-10-01 16:00:00', '2026-10-01 16:30:00'),
(2, NULL, '2026-10-01 16:30:00', '2026-10-01 17:00:00');

-- Bloque 3: Dr. Ruiz en ICV, 2026-10-02 09:00 a 12:00 (Pediatría)
INSERT IGNORE INTO availability_blocks (id, professional_id, location_id, available_date, start_time, end_time, active) VALUES
(3, 3, 2, '2026-10-02', '09:00:00', '12:00:00', TRUE);

INSERT IGNORE INTO professional_slots (availability_block_id, appointment_id, start_at, end_at) VALUES
(3, NULL, '2026-10-02 09:00:00', '2026-10-02 09:30:00'),
(3, NULL, '2026-10-02 09:30:00', '2026-10-02 10:00:00'),
(3, NULL, '2026-10-02 10:00:00', '2026-10-02 10:30:00'),
(3, NULL, '2026-10-02 10:30:00', '2026-10-02 11:00:00'),
(3, NULL, '2026-10-02 11:00:00', '2026-10-02 11:30:00'),
(3, NULL, '2026-10-02 11:30:00', '2026-10-02 12:00:00');
