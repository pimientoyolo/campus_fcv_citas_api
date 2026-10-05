-- V5: Regímenes, EPS, Planes y Afiliaciones de Pacientes (RF-04, RF-05, RF-06)

CREATE TABLE regimens (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT IGNORE INTO regimens (id, code, name, active) VALUES
(1, 'CONTRIBUTIVO', 'Régimen Contributivo', TRUE),
(2, 'SUBSIDIADO', 'Régimen Subsidiado', TRUE),
(3, 'PARTICULAR', 'Particular / Privado', TRUE);

CREATE TABLE eps_entities (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT IGNORE INTO eps_entities (id, code, name, active) VALUES
(1, 'SURA', 'EPS SURA', TRUE),
(2, 'SANITAS', 'EPS Sanitas', TRUE),
(3, 'COMPENSAR', 'Compensar EPS', TRUE),
(4, 'NUEVA_EPS', 'Nueva EPS', TRUE),
(5, 'SALUD_TOTAL', 'Salud Total EPS', TRUE);

CREATE TABLE eps_plans (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    eps_id SMALLINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (eps_id) REFERENCES eps_entities(id),
    CONSTRAINT uk_eps_plan_code UNIQUE (eps_id, code)
);

INSERT IGNORE INTO eps_plans (id, eps_id, code, name, active) VALUES
(1, 1, 'SURA_POS', 'Plan Obligatorio de Salud (POS)', TRUE),
(2, 1, 'SURA_PREF', 'Plan Preferencial SURA', TRUE),
(3, 2, 'SAN_BASICO', 'Plan Básico Sanitas', TRUE),
(4, 2, 'SAN_PREMIUM', 'Plan Premium Sanitas', TRUE),
(5, 3, 'COMP_TRAD', 'Compensar Tradicional', TRUE);

CREATE TABLE user_affiliations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    eps_id SMALLINT NOT NULL,
    eps_plan_id SMALLINT NOT NULL,
    regimen_id SMALLINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES app_users(id),
    FOREIGN KEY (eps_id) REFERENCES eps_entities(id),
    FOREIGN KEY (eps_plan_id) REFERENCES eps_plans(id),
    FOREIGN KEY (regimen_id) REFERENCES regimens(id),
    CONSTRAINT uk_user_affiliation UNIQUE (user_id, eps_id, eps_plan_id, regimen_id)
);

-- Afiliación sintética de prueba para el paciente de laboratorio
INSERT IGNORE INTO user_affiliations (id, user_id, eps_id, eps_plan_id, regimen_id, active) VALUES
(1, 104, 1, 2, 1, TRUE);
