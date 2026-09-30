-- ============================================================================
-- V3__seed_test_appointments_and_blocks.sql
-- Datos sintéticos enriquecidos para pruebas de agendamiento y gestión de citas FCV
-- Permite validar en Docker todos los estados: APPROVED, REQUESTED, CANCELLED,
-- REJECTED, COMPLETED y NO_SHOW con múltiples pacientes, médicos y sedes.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Pacientes Sintéticos Adicionales para Pruebas
-- Contraseña para todos: User123* (hash bcrypt $2a$12$cXROFTFaEqh/uogkkHMm5e2MbT7CNZlHHwG1/s1yk9sn14qA08cmG)
-- ----------------------------------------------------------------------------
INSERT IGNORE INTO app_users (id, first_name, last_name, document_type, document_number, email, phone, password_hash, active) VALUES
(110, 'Juan Carlos', 'Pérez', 'CC', '1000000010', 'juan.perez@fcv.test', '+57 310 123 4567', '$2a$12$cXROFTFaEqh/uogkkHMm5e2MbT7CNZlHHwG1/s1yk9sn14qA08cmG', TRUE),
(111, 'María Camila', 'Gómez', 'CC', '1000000011', 'maria.gomez@fcv.test', '+57 311 234 5678', '$2a$12$cXROFTFaEqh/uogkkHMm5e2MbT7CNZlHHwG1/s1yk9sn14qA08cmG', TRUE),
(112, 'David Santiago', 'Rodríguez', 'CC', '1000000012', 'david.rodriguez@fcv.test', '+57 312 345 6789', '$2a$12$cXROFTFaEqh/uogkkHMm5e2MbT7CNZlHHwG1/s1yk9sn14qA08cmG', TRUE),
(113, 'Carmen Elena', 'Silva', 'CC', '1000000013', 'carmen.silva@fcv.test', '+57 313 456 7890', '$2a$12$cXROFTFaEqh/uogkkHMm5e2MbT7CNZlHHwG1/s1yk9sn14qA08cmG', TRUE);

INSERT IGNORE INTO user_roles (user_id, role_code) VALUES
(110, 'USER'),
(111, 'USER'),
(112, 'USER'),
(113, 'USER');

-- ----------------------------------------------------------------------------
-- 2. Bloques de Disponibilidad Adicionales (Pasados, Presentes y Futuros)
-- ----------------------------------------------------------------------------
-- Dr. Carlos Mendoza (id 1, Medicina General, 30 min)
INSERT IGNORE INTO availability_blocks (id, professional_id, location_id, available_date, start_time, end_time, active) VALUES
(10, 1, 1, '2026-09-25', '08:00:00', '12:00:00', TRUE), -- HIC
(11, 1, 2, '2026-09-28', '08:00:00', '12:00:00', TRUE), -- ICV
(12, 1, 1, '2026-10-02', '08:00:00', '12:00:00', TRUE), -- HIC
(13, 1, 2, '2026-10-05', '08:00:00', '12:00:00', TRUE), -- ICV
(14, 1, 1, '2026-10-08', '08:00:00', '12:00:00', TRUE); -- HIC

-- Dra. Sofía Castro (id 2, Cardiología, 60 min, Sede HIC = 1)
INSERT IGNORE INTO availability_blocks (id, professional_id, location_id, available_date, start_time, end_time, active) VALUES
(15, 2, 1, '2026-09-22', '14:00:00', '18:00:00', TRUE),
(16, 2, 1, '2026-09-29', '14:00:00', '18:00:00', TRUE),
(17, 2, 1, '2026-10-02', '14:00:00', '18:00:00', TRUE),
(18, 2, 1, '2026-10-06', '14:00:00', '18:00:00', TRUE),
(19, 2, 1, '2026-10-09', '14:00:00', '18:00:00', TRUE);

-- Dr. Andrés Ruiz (id 3, Pediatría, 30 min, Sede ICV = 2)
INSERT IGNORE INTO availability_blocks (id, professional_id, location_id, available_date, start_time, end_time, active) VALUES
(20, 3, 2, '2026-09-24', '09:00:00', '13:00:00', TRUE),
(21, 3, 2, '2026-09-30', '09:00:00', '13:00:00', TRUE),
(22, 3, 2, '2026-10-03', '09:00:00', '13:00:00', TRUE),
(23, 3, 2, '2026-10-07', '09:00:00', '13:00:00', TRUE),
(24, 3, 2, '2026-10-10', '09:00:00', '13:00:00', TRUE);

-- ----------------------------------------------------------------------------
-- 3. Citas Sintéticas (IDs del 101 al 134 para evitar colisión con citas previas)
-- ----------------------------------------------------------------------------
INSERT IGNORE INTO appointments (id, patient_user_id, professional_id, location_id, specialty_id, status_id, scheduled_start_at, scheduled_end_at, rejection_reason, created_at, updated_at) VALUES
-- Grupo COMPLETED (Atendidas exitosamente en fechas pasadas)
(101, 104, 1, 1, 1, 5, '2026-09-25 08:00:00', '2026-09-25 08:30:00', NULL, '2026-09-20 09:00:00', '2026-09-25 08:35:00'),
(102, 104, 2, 1, 2, 5, '2026-09-22 14:00:00', '2026-09-22 15:00:00', NULL, '2026-09-18 10:30:00', '2026-09-22 15:05:00'),
(103, 110, 1, 2, 1, 5, '2026-09-28 08:00:00', '2026-09-28 08:30:00', NULL, '2026-09-21 11:00:00', '2026-09-28 08:32:00'),
(104, 111, 3, 2, 3, 5, '2026-09-24 09:00:00', '2026-09-24 09:30:00', NULL, '2026-09-20 14:15:00', '2026-09-24 09:35:00'),
(105, 112, 2, 1, 2, 5, '2026-09-29 14:00:00', '2026-09-29 15:00:00', NULL, '2026-09-23 16:00:00', '2026-09-29 15:02:00'),
(106, 113, 1, 1, 1, 5, '2026-09-25 09:00:00', '2026-09-25 09:30:00', NULL, '2026-09-20 15:45:00', '2026-09-25 09:33:00'),

-- Grupo NO_SHOW (Inasistencias registradas por los médicos en fechas pasadas)
(107, 104, 1, 1, 1, 6, '2026-09-25 10:00:00', '2026-09-25 10:30:00', 'Paciente no se presentó a consulta ni notificó previamente', '2026-09-21 08:20:00', '2026-09-25 10:45:00'),
(108, 110, 2, 1, 2, 6, '2026-09-22 16:00:00', '2026-09-22 17:00:00', 'Inasistencia sin cancelación oportuna', '2026-09-17 12:00:00', '2026-09-22 17:15:00'),
(109, 111, 3, 2, 3, 6, '2026-09-24 11:00:00', '2026-09-24 11:30:00', 'Paciente reportó inconveniente de transporte post-horario', '2026-09-19 13:00:00', '2026-09-24 11:40:00'),
(110, 112, 1, 2, 1, 6, '2026-09-28 10:00:00', '2026-09-28 10:30:00', 'No se presentó al llamado en sala de espera', '2026-09-22 10:10:00', '2026-09-28 10:40:00'),

-- Grupo CANCELLED (Canceladas por el paciente o admin - slots liberados)
(111, 104, 1, 1, 1, 4, '2026-10-02 08:00:00', '2026-10-02 08:30:00', NULL, '2026-09-28 09:00:00', '2026-09-29 16:30:00'),
(112, 104, 2, 1, 2, 4, '2026-10-02 14:00:00', '2026-10-02 15:00:00', NULL, '2026-09-27 10:15:00', '2026-09-29 18:00:00'),
(113, 110, 3, 2, 3, 4, '2026-10-03 09:00:00', '2026-10-03 09:30:00', NULL, '2026-09-28 11:30:00', '2026-09-30 08:00:00'),
(114, 111, 1, 2, 1, 4, '2026-10-05 08:30:00', '2026-10-05 09:00:00', NULL, '2026-09-29 14:00:00', '2026-09-30 09:15:00'),
(115, 113, 2, 1, 2, 4, '2026-10-06 14:00:00', '2026-10-06 15:00:00', NULL, '2026-09-28 15:45:00', '2026-09-30 10:00:00'),

-- Grupo REJECTED (Rechazadas por Administración con motivo obligatorio - slots liberados)
(116, 104, 2, 1, 2, 3, '2026-10-02 16:00:00', '2026-10-02 17:00:00', 'Falta orden médica de remisión con vigencia menor a 30 días expedida por Medicina General.', '2026-09-27 11:00:00', '2026-09-28 16:00:00'),
(117, 110, 3, 2, 3, 3, '2026-10-03 10:30:00', '2026-10-03 11:00:00', 'Documento de identidad adjunto ilegible; favor renovar registro civil o tarjeta de identidad.', '2026-09-28 14:20:00', '2026-09-29 10:30:00'),
(118, 111, 2, 1, 2, 3, '2026-10-06 16:00:00', '2026-10-06 17:00:00', 'No cuenta con electrocardiograma previo requerido para valoración por especialista en Cardiología.', '2026-09-28 16:00:00', '2026-09-29 15:00:00'),
(119, 112, 3, 2, 3, 3, '2026-10-07 09:30:00', '2026-10-07 10:00:00', 'Paciente supera el rango de edad para consulta pediátrica general; remitir a Medicina General.', '2026-09-29 09:30:00', '2026-09-30 11:00:00'),
(120, 113, 2, 1, 2, 3, '2026-10-09 14:00:00', '2026-10-09 15:00:00', 'Solicitud duplicada para la misma especialidad en el mismo mes calendario.', '2026-09-29 17:00:00', '2026-09-30 12:00:00'),

-- Grupo REQUESTED (Solicitudes pendientes de revisión administrativa - Bandeja Admin)
(121, 104, 2, 1, 2, 1, '2026-10-06 15:00:00', '2026-10-06 16:00:00', NULL, '2026-09-30 08:30:00', '2026-09-30 08:30:00'),
(122, 104, 3, 2, 3, 1, '2026-10-07 10:00:00', '2026-10-07 10:30:00', NULL, '2026-09-30 09:00:00', '2026-09-30 09:00:00'),
(123, 110, 2, 1, 2, 1, '2026-10-09 15:00:00', '2026-10-09 16:00:00', NULL, '2026-09-30 09:45:00', '2026-09-30 09:45:00'),
(124, 110, 3, 2, 3, 1, '2026-10-07 11:00:00', '2026-10-07 11:30:00', NULL, '2026-09-30 10:15:00', '2026-09-30 10:15:00'),
(125, 111, 2, 1, 2, 1, '2026-10-09 16:00:00', '2026-10-09 17:00:00', NULL, '2026-09-30 11:00:00', '2026-09-30 11:00:00'),
(126, 112, 3, 2, 3, 1, '2026-10-10 09:00:00', '2026-10-10 09:30:00', NULL, '2026-09-30 11:30:00', '2026-09-30 11:30:00'),
(127, 113, 3, 2, 3, 1, '2026-10-10 10:00:00', '2026-10-10 10:30:00', NULL, '2026-09-30 12:15:00', '2026-09-30 12:15:00'),

-- Grupo APPROVED (Citas programadas y confirmadas para fechas vigentes)
(128, 104, 1, 1, 1, 2, '2026-10-02 09:00:00', '2026-10-02 09:30:00', NULL, '2026-09-29 08:00:00', '2026-09-29 08:00:00'),
(129, 104, 1, 2, 1, 2, '2026-10-05 09:00:00', '2026-10-05 09:30:00', NULL, '2026-09-29 08:30:00', '2026-09-29 08:30:00'),
(130, 110, 1, 1, 1, 2, '2026-10-02 10:00:00', '2026-10-02 10:30:00', NULL, '2026-09-29 09:15:00', '2026-09-29 09:15:00'),
(131, 111, 1, 2, 1, 2, '2026-10-05 10:00:00', '2026-10-05 10:30:00', NULL, '2026-09-29 10:00:00', '2026-09-29 10:00:00'),
(132, 112, 1, 1, 1, 2, '2026-10-08 08:30:00', '2026-10-08 09:00:00', NULL, '2026-09-29 11:20:00', '2026-09-29 11:20:00'),
(133, 113, 1, 1, 1, 2, '2026-10-08 10:00:00', '2026-10-08 10:30:00', NULL, '2026-09-29 13:00:00', '2026-09-29 13:00:00'),
(134, 104, 2, 1, 2, 2, '2026-10-02 15:00:00', '2026-10-02 16:00:00', NULL, '2026-09-28 10:00:00', '2026-09-29 14:00:00');

-- ----------------------------------------------------------------------------
-- 4. Slots de Profesionales Asociados a los Bloques (Total 120 slots)
-- Los slots de citas activas (APPROVED, REQUESTED, COMPLETED, NO_SHOW) tienen appointment_id
-- Los slots de citas CANCELLED, REJECTED o aún no agendadas tienen appointment_id = NULL
-- ----------------------------------------------------------------------------
-- Bloque 10: Dr. Mendoza, HIC, 2026-09-25 08:00 - 12:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(100, 10, 101,  '2026-09-25 08:00:00', '2026-09-25 08:30:00'), -- COMPLETED (Cita 101)
(101, 10, NULL, '2026-09-25 08:30:00', '2026-09-25 09:00:00'), -- Libre
(102, 10, 106,  '2026-09-25 09:00:00', '2026-09-25 09:30:00'), -- COMPLETED (Cita 106)
(103, 10, NULL, '2026-09-25 09:30:00', '2026-09-25 10:00:00'), -- Libre
(104, 10, 107,  '2026-09-25 10:00:00', '2026-09-25 10:30:00'), -- NO_SHOW (Cita 107)
(105, 10, NULL, '2026-09-25 10:30:00', '2026-09-25 11:00:00'), -- Libre
(106, 10, NULL, '2026-09-25 11:00:00', '2026-09-25 11:30:00'), -- Libre
(107, 10, NULL, '2026-09-25 11:30:00', '2026-09-25 12:00:00'); -- Libre

-- Bloque 11: Dr. Mendoza, ICV, 2026-09-28 08:00 - 12:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(108, 11, 103,  '2026-09-28 08:00:00', '2026-09-28 08:30:00'), -- COMPLETED (Cita 103)
(109, 11, NULL, '2026-09-28 08:30:00', '2026-09-28 09:00:00'),
(110, 11, NULL, '2026-09-28 09:00:00', '2026-09-28 09:30:00'),
(111, 11, NULL, '2026-09-28 09:30:00', '2026-09-28 10:00:00'),
(112, 11, 110,  '2026-09-28 10:00:00', '2026-09-28 10:30:00'), -- NO_SHOW (Cita 110)
(113, 11, NULL, '2026-09-28 10:30:00', '2026-09-28 11:00:00'),
(114, 11, NULL, '2026-09-28 11:00:00', '2026-09-28 11:30:00'),
(115, 11, NULL, '2026-09-28 11:30:00', '2026-09-28 12:00:00');

-- Bloque 12: Dr. Mendoza, HIC, 2026-10-02 08:00 - 12:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(116, 12, NULL, '2026-10-02 08:00:00', '2026-10-02 08:30:00'), -- Liberado por Cancelación (Cita 111)
(117, 12, NULL, '2026-10-02 08:30:00', '2026-10-02 09:00:00'),
(118, 12, 128,  '2026-10-02 09:00:00', '2026-10-02 09:30:00'), -- APPROVED (Cita 128)
(119, 12, NULL, '2026-10-02 09:30:00', '2026-10-02 10:00:00'),
(120, 12, 130,  '2026-10-02 10:00:00', '2026-10-02 10:30:00'), -- APPROVED (Cita 130)
(121, 12, NULL, '2026-10-02 10:30:00', '2026-10-02 11:00:00'),
(122, 12, NULL, '2026-10-02 11:00:00', '2026-10-02 11:30:00'),
(123, 12, NULL, '2026-10-02 11:30:00', '2026-10-02 12:00:00');

-- Bloque 13: Dr. Mendoza, ICV, 2026-10-05 08:00 - 12:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(124, 13, NULL, '2026-10-05 08:00:00', '2026-10-05 08:30:00'),
(125, 13, NULL, '2026-10-05 08:30:00', '2026-10-05 09:00:00'), -- Liberado por Cancelación (Cita 114)
(126, 13, 129,  '2026-10-05 09:00:00', '2026-10-05 09:30:00'), -- APPROVED (Cita 129)
(127, 13, NULL, '2026-10-05 09:30:00', '2026-10-05 10:00:00'),
(128, 13, 131,  '2026-10-05 10:00:00', '2026-10-05 10:30:00'), -- APPROVED (Cita 131)
(129, 13, NULL, '2026-10-05 10:30:00', '2026-10-05 11:00:00'),
(130, 13, NULL, '2026-10-05 11:00:00', '2026-10-05 11:30:00'),
(131, 13, NULL, '2026-10-05 11:30:00', '2026-10-05 12:00:00');

-- Bloque 14: Dr. Mendoza, HIC, 2026-10-08 08:00 - 12:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(132, 14, NULL, '2026-10-08 08:00:00', '2026-10-08 08:30:00'),
(133, 14, 132,  '2026-10-08 08:30:00', '2026-10-08 09:00:00'), -- APPROVED (Cita 132)
(134, 14, NULL, '2026-10-08 09:00:00', '2026-10-08 09:30:00'),
(135, 14, NULL, '2026-10-08 09:30:00', '2026-10-08 10:00:00'),
(136, 14, 133,  '2026-10-08 10:00:00', '2026-10-08 10:30:00'), -- APPROVED (Cita 133)
(137, 14, NULL, '2026-10-08 10:30:00', '2026-10-08 11:00:00'),
(138, 14, NULL, '2026-10-08 11:00:00', '2026-10-08 11:30:00'),
(139, 14, NULL, '2026-10-08 11:30:00', '2026-10-08 12:00:00');

-- Bloque 15: Dra. Castro (Cardiología - 60 min pares de slots), HIC, 2026-09-22 14:00 - 18:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(140, 15, 102,  '2026-09-22 14:00:00', '2026-09-22 14:30:00'), -- COMPLETED (Cita 102 slot 1/2)
(141, 15, 102,  '2026-09-22 14:30:00', '2026-09-22 15:00:00'), -- COMPLETED (Cita 102 slot 2/2)
(142, 15, NULL, '2026-09-22 15:00:00', '2026-09-22 15:30:00'),
(143, 15, NULL, '2026-09-22 15:30:00', '2026-09-22 16:00:00'),
(144, 15, 108,  '2026-09-22 16:00:00', '2026-09-22 16:30:00'), -- NO_SHOW (Cita 108 slot 1/2)
(145, 15, 108,  '2026-09-22 16:30:00', '2026-09-22 17:00:00'), -- NO_SHOW (Cita 108 slot 2/2)
(146, 15, NULL, '2026-09-22 17:00:00', '2026-09-22 17:30:00'),
(147, 15, NULL, '2026-09-22 17:30:00', '2026-09-22 18:00:00');

-- Bloque 16: Dra. Castro, HIC, 2026-09-29 14:00 - 18:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(148, 16, 105,  '2026-09-29 14:00:00', '2026-09-29 14:30:00'), -- COMPLETED (Cita 105)
(149, 16, 105,  '2026-09-29 14:30:00', '2026-09-29 15:00:00'),
(150, 16, NULL, '2026-09-29 15:00:00', '2026-09-29 15:30:00'),
(151, 16, NULL, '2026-09-29 15:30:00', '2026-09-29 16:00:00'),
(152, 16, NULL, '2026-09-29 16:00:00', '2026-09-29 16:30:00'),
(153, 16, NULL, '2026-09-29 16:30:00', '2026-09-29 17:00:00'),
(154, 16, NULL, '2026-09-29 17:00:00', '2026-09-29 17:30:00'),
(155, 16, NULL, '2026-09-29 17:30:00', '2026-09-29 18:00:00');

-- Bloque 17: Dra. Castro, HIC, 2026-10-02 14:00 - 18:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(156, 17, NULL, '2026-10-02 14:00:00', '2026-10-02 14:30:00'), -- Liberado por Cancelación (Cita 112)
(157, 17, NULL, '2026-10-02 14:30:00', '2026-10-02 15:00:00'),
(158, 17, 134,  '2026-10-02 15:00:00', '2026-10-02 15:30:00'), -- APPROVED (Cita 134)
(159, 17, 134,  '2026-10-02 15:30:00', '2026-10-02 16:00:00'),
(160, 17, NULL, '2026-10-02 16:00:00', '2026-10-02 16:30:00'), -- Liberado por Rechazo (Cita 116)
(161, 17, NULL, '2026-10-02 16:30:00', '2026-10-02 17:00:00'),
(162, 17, NULL, '2026-10-02 17:00:00', '2026-10-02 17:30:00'),
(163, 17, NULL, '2026-10-02 17:30:00', '2026-10-02 18:00:00');

-- Bloque 18: Dra. Castro, HIC, 2026-10-06 14:00 - 18:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(164, 18, NULL, '2026-10-06 14:00:00', '2026-10-06 14:30:00'), -- Liberado por Cancelación (Cita 115)
(165, 18, NULL, '2026-10-06 14:30:00', '2026-10-06 15:00:00'),
(166, 18, 121,  '2026-10-06 15:00:00', '2026-10-06 15:30:00'), -- REQUESTED (Cita 121)
(167, 18, 121,  '2026-10-06 15:30:00', '2026-10-06 16:00:00'),
(168, 18, NULL, '2026-10-06 16:00:00', '2026-10-06 16:30:00'), -- Liberado por Rechazo (Cita 118)
(169, 18, NULL, '2026-10-06 16:30:00', '2026-10-06 17:00:00'),
(170, 18, NULL, '2026-10-06 17:00:00', '2026-10-06 17:30:00'),
(171, 18, NULL, '2026-10-06 17:30:00', '2026-10-06 18:00:00');

-- Bloque 19: Dra. Castro, HIC, 2026-10-09 14:00 - 18:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(172, 19, NULL, '2026-10-09 14:00:00', '2026-10-09 14:30:00'), -- Liberado por Rechazo (Cita 120)
(173, 19, NULL, '2026-10-09 14:30:00', '2026-10-09 15:00:00'),
(174, 19, 123,  '2026-10-09 15:00:00', '2026-10-09 15:30:00'), -- REQUESTED (Cita 123)
(175, 19, 123,  '2026-10-09 15:30:00', '2026-10-09 16:00:00'),
(176, 19, 125,  '2026-10-09 16:00:00', '2026-10-09 16:30:00'), -- REQUESTED (Cita 125)
(177, 19, 125,  '2026-10-09 16:30:00', '2026-10-09 17:00:00'),
(178, 19, NULL, '2026-10-09 17:00:00', '2026-10-09 17:30:00'),
(179, 19, NULL, '2026-10-09 17:30:00', '2026-10-09 18:00:00');

-- Bloque 20: Dr. Ruiz, ICV, 2026-09-24 09:00 - 13:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(180, 20, 104,  '2026-09-24 09:00:00', '2026-09-24 09:30:00'), -- COMPLETED (Cita 104)
(181, 20, NULL, '2026-09-24 09:30:00', '2026-09-24 10:00:00'),
(182, 20, NULL, '2026-09-24 10:00:00', '2026-09-24 10:30:00'),
(183, 20, NULL, '2026-09-24 10:30:00', '2026-09-24 11:00:00'),
(184, 20, 109,  '2026-09-24 11:00:00', '2026-09-24 11:30:00'), -- NO_SHOW (Cita 109)
(185, 20, NULL, '2026-09-24 11:30:00', '2026-09-24 12:00:00'),
(186, 20, NULL, '2026-09-24 12:00:00', '2026-09-24 12:30:00'),
(187, 20, NULL, '2026-09-24 12:30:00', '2026-09-24 13:00:00');

-- Bloque 21: Dr. Ruiz, ICV, 2026-09-30 09:00 - 13:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(188, 21, NULL, '2026-09-30 09:00:00', '2026-09-30 09:30:00'),
(189, 21, NULL, '2026-09-30 09:30:00', '2026-09-30 10:00:00'),
(190, 21, NULL, '2026-09-30 10:00:00', '2026-09-30 10:30:00'),
(191, 21, NULL, '2026-09-30 10:30:00', '2026-09-30 11:00:00'),
(192, 21, NULL, '2026-09-30 11:00:00', '2026-09-30 11:30:00'),
(193, 21, NULL, '2026-09-30 11:30:00', '2026-09-30 12:00:00'),
(194, 21, NULL, '2026-09-30 12:00:00', '2026-09-30 12:30:00'),
(195, 21, NULL, '2026-09-30 12:30:00', '2026-09-30 13:00:00');

-- Bloque 22: Dr. Ruiz, ICV, 2026-10-03 09:00 - 13:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(196, 22, NULL, '2026-10-03 09:00:00', '2026-10-03 09:30:00'), -- Liberado por Cancelación (Cita 113)
(197, 22, NULL, '2026-10-03 09:30:00', '2026-10-03 10:00:00'),
(198, 22, NULL, '2026-10-03 10:00:00', '2026-10-03 10:30:00'),
(199, 22, NULL, '2026-10-03 10:30:00', '2026-10-03 11:00:00'), -- Liberado por Rechazo (Cita 117)
(200, 22, NULL, '2026-10-03 11:00:00', '2026-10-03 11:30:00'),
(201, 22, NULL, '2026-10-03 11:30:00', '2026-10-03 12:00:00'),
(202, 22, NULL, '2026-10-03 12:00:00', '2026-10-03 12:30:00'),
(203, 22, NULL, '2026-10-03 12:30:00', '2026-10-03 13:00:00');

-- Bloque 23: Dr. Ruiz, ICV, 2026-10-07 09:00 - 13:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(204, 23, NULL, '2026-10-07 09:00:00', '2026-10-07 09:30:00'),
(205, 23, NULL, '2026-10-07 09:30:00', '2026-10-07 10:00:00'), -- Liberado por Rechazo (Cita 119)
(206, 23, 122,  '2026-10-07 10:00:00', '2026-10-07 10:30:00'), -- REQUESTED (Cita 122)
(207, 23, NULL, '2026-10-07 10:30:00', '2026-10-07 11:00:00'),
(208, 23, 124,  '2026-10-07 11:00:00', '2026-10-07 11:30:00'), -- REQUESTED (Cita 124)
(209, 23, NULL, '2026-10-07 11:30:00', '2026-10-07 12:00:00'),
(210, 23, NULL, '2026-10-07 12:00:00', '2026-10-07 12:30:00'),
(211, 23, NULL, '2026-10-07 12:30:00', '2026-10-07 13:00:00');

-- Bloque 24: Dr. Ruiz, ICV, 2026-10-10 09:00 - 13:00
INSERT IGNORE INTO professional_slots (id, availability_block_id, appointment_id, start_at, end_at) VALUES
(212, 24, 126,  '2026-10-10 09:00:00', '2026-10-10 09:30:00'), -- REQUESTED (Cita 126)
(213, 24, NULL, '2026-10-10 09:30:00', '2026-10-10 10:00:00'),
(214, 24, 127,  '2026-10-10 10:00:00', '2026-10-10 10:30:00'), -- REQUESTED (Cita 127)
(215, 24, NULL, '2026-10-10 10:30:00', '2026-10-10 11:00:00'),
(216, 24, NULL, '2026-10-10 11:00:00', '2026-10-10 11:30:00'),
(217, 24, NULL, '2026-10-10 11:30:00', '2026-10-10 12:00:00'),
(218, 24, NULL, '2026-10-10 12:00:00', '2026-10-10 12:30:00'),
(219, 24, NULL, '2026-10-10 12:30:00', '2026-10-10 13:00:00');

-- ----------------------------------------------------------------------------
-- 5. Historial de Auditoría de Estados (Auditoría completa de transiciones)
-- ----------------------------------------------------------------------------
INSERT IGNORE INTO appointment_status_history (id, appointment_id, status_id, changed_by_user_id, change_source, changed_at, reason) VALUES
-- Citas COMPLETED
(100, 101, 2, 104, 'USER', '2026-09-20 09:00:00', 'Cita general auto-aprobada'),
(101, 101, 5, 101, 'PROFESSIONAL', '2026-09-25 08:35:00', 'Atención médica completada con éxito'),

(102, 102, 1, 104, 'USER', '2026-09-18 10:30:00', 'Solicitud de cita especializada'),
(103, 102, 2, 100, 'ADMIN', '2026-09-19 11:00:00', 'Aprobada por administración'),
(104, 102, 5, 102, 'PROFESSIONAL', '2026-09-22 15:05:00', 'Valoración cardiológica realizada'),

(105, 103, 2, 110, 'USER', '2026-09-21 11:00:00', 'Cita general auto-aprobada'),
(106, 103, 5, 101, 'PROFESSIONAL', '2026-09-28 08:32:00', 'Atención completada satisfactoriamente'),

(107, 104, 1, 111, 'USER', '2026-09-20 14:15:00', 'Solicitud de cita pediátrica'),
(108, 104, 2, 100, 'ADMIN', '2026-09-21 09:00:00', 'Aprobada con orden médica verificada'),
(109, 104, 5, 103, 'PROFESSIONAL', '2026-09-24 09:35:00', 'Control pediátrico completado'),

(110, 105, 1, 112, 'USER', '2026-09-23 16:00:00', 'Solicitud de cita especializada'),
(111, 105, 2, 100, 'ADMIN', '2026-09-24 08:30:00', 'Aprobada por administración'),
(112, 105, 5, 102, 'PROFESSIONAL', '2026-09-29 15:02:00', 'Atención cardiológica completada'),

(113, 106, 2, 113, 'USER', '2026-09-20 15:45:00', 'Cita general auto-aprobada'),
(114, 106, 5, 101, 'PROFESSIONAL', '2026-09-25 09:33:00', 'Consulta médica finalizada'),

-- Citas NO_SHOW
(115, 107, 2, 104, 'USER', '2026-09-21 08:20:00', 'Cita general auto-aprobada'),
(116, 107, 6, 101, 'PROFESSIONAL', '2026-09-25 10:45:00', 'Paciente no se presentó a consulta ni notificó previamente'),

(117, 108, 1, 110, 'USER', '2026-09-17 12:00:00', 'Solicitud de cita especializada'),
(118, 108, 2, 100, 'ADMIN', '2026-09-18 10:00:00', 'Aprobada por administración'),
(119, 108, 6, 102, 'PROFESSIONAL', '2026-09-22 17:15:00', 'Inasistencia sin cancelación oportuna'),

(120, 109, 1, 111, 'USER', '2026-09-19 13:00:00', 'Solicitud de cita pediátrica'),
(121, 109, 2, 100, 'ADMIN', '2026-09-20 11:00:00', 'Aprobada por administración'),
(122, 109, 6, 103, 'PROFESSIONAL', '2026-09-24 11:40:00', 'Paciente reportó inconveniente de transporte post-horario'),

(123, 110, 2, 112, 'USER', '2026-09-22 10:10:00', 'Cita general auto-aprobada'),
(124, 110, 6, 101, 'PROFESSIONAL', '2026-09-28 10:40:00', 'No se presentó al llamado en sala de espera'),

-- Citas CANCELLED
(125, 111, 2, 104, 'USER', '2026-09-28 09:00:00', 'Cita general auto-aprobada'),
(126, 111, 4, 104, 'USER', '2026-09-29 16:30:00', 'Cancelada por el paciente'),

(127, 112, 1, 104, 'USER', '2026-09-27 10:15:00', 'Solicitud de cita especializada'),
(128, 112, 2, 100, 'ADMIN', '2026-09-28 08:30:00', 'Aprobada por administración'),
(129, 112, 4, 104, 'USER', '2026-09-29 18:00:00', 'Cancelada por el paciente por viaje laboral'),

(130, 113, 1, 110, 'USER', '2026-09-28 11:30:00', 'Solicitud de cita pediátrica'),
(131, 113, 4, 110, 'USER', '2026-09-30 08:00:00', 'Cancelada antes de aprobación'),

(132, 114, 2, 111, 'USER', '2026-09-29 14:00:00', 'Cita general auto-aprobada'),
(133, 114, 4, 111, 'USER', '2026-09-30 09:15:00', 'Cancelada por cambio de horario de trabajo'),

(134, 115, 1, 113, 'USER', '2026-09-28 15:45:00', 'Solicitud de cita cardiológica'),
(135, 115, 4, 100, 'ADMIN', '2026-09-30 10:00:00', 'Cancelada por administración a solicitud del usuario'),

-- Citas REJECTED
(136, 116, 1, 104, 'USER', '2026-09-27 11:00:00', 'Solicitud de cita especializada'),
(137, 116, 3, 100, 'ADMIN', '2026-09-28 16:00:00', 'Falta orden médica de remisión con vigencia menor a 30 días expedida por Medicina General.'),

(138, 117, 1, 110, 'USER', '2026-09-28 14:20:00', 'Solicitud de cita pediátrica'),
(139, 117, 3, 100, 'ADMIN', '2026-09-29 10:30:00', 'Documento de identidad adjunto ilegible; favor renovar registro civil o tarjeta de identidad.'),

(140, 118, 1, 111, 'USER', '2026-09-28 16:00:00', 'Solicitud de cita cardiológica'),
(141, 118, 3, 100, 'ADMIN', '2026-09-29 15:00:00', 'No cuenta con electrocardiograma previo requerido para valoración por especialista en Cardiología.'),

(142, 119, 1, 112, 'USER', '2026-09-29 09:30:00', 'Solicitud de cita pediátrica'),
(143, 119, 3, 100, 'ADMIN', '2026-09-30 11:00:00', 'Paciente supera el rango de edad para consulta pediátrica general; remitir a Medicina General.'),

(144, 120, 1, 113, 'USER', '2026-09-29 17:00:00', 'Solicitud de cita cardiológica'),
(145, 120, 3, 100, 'ADMIN', '2026-09-30 12:00:00', 'Solicitud duplicada para la misma especialidad en el mismo mes calendario.'),

-- Citas REQUESTED (Pendientes de Aprobación)
(146, 121, 1, 104, 'USER', '2026-09-30 08:30:00', 'Solicitud de valoración por Cardiología'),
(147, 122, 1, 104, 'USER', '2026-09-30 09:00:00', 'Solicitud de control por Pediatría'),
(148, 123, 1, 110, 'USER', '2026-09-30 09:45:00', 'Solicitud de cita especializada en Cardiología'),
(149, 124, 1, 110, 'USER', '2026-09-30 10:15:00', 'Solicitud de valoración pediátrica'),
(150, 125, 1, 111, 'USER', '2026-09-30 11:00:00', 'Solicitud de chequeo por Cardiología'),
(151, 126, 1, 112, 'USER', '2026-09-30 11:30:00', 'Solicitud de control pediátrico'),
(152, 127, 1, 113, 'USER', '2026-09-30 12:15:00', 'Solicitud de consulta por Pediatría'),

-- Citas APPROVED (Vigentes)
(153, 128, 2, 104, 'USER', '2026-09-29 08:00:00', 'Cita general auto-aprobada'),
(154, 129, 2, 104, 'USER', '2026-09-29 08:30:00', 'Cita general auto-aprobada'),
(155, 130, 2, 110, 'USER', '2026-09-29 09:15:00', 'Cita general auto-aprobada'),
(156, 131, 2, 111, 'USER', '2026-09-29 10:00:00', 'Cita general auto-aprobada'),
(157, 132, 2, 112, 'USER', '2026-09-29 11:20:00', 'Cita general auto-aprobada'),
(158, 133, 2, 113, 'USER', '2026-09-29 13:00:00', 'Cita general auto-aprobada'),
(159, 134, 1, 104, 'USER', '2026-09-28 10:00:00', 'Solicitud de cita especializada en Cardiología'),
(160, 134, 2, 100, 'ADMIN', '2026-09-29 14:00:00', 'Aprobada con orden médica vigente por administración');
