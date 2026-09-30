---
id: EP-002
tipo: epica
titulo: Gestión y agendamiento de citas
estado: Implementado
historias:
  - "[[HU-004-consultar-disponibilidad]]"
  - "[[HU-005-agendar-cita]]"
  - "[[HU-006-gestion-administrativa-y-medica]]"
dependencias:
  - "[[EP-001-acceso-de-usuarios]]"
---

# EP-002 — Gestión y agendamiento de citas (S3)

## Objetivo y valor
Permitir a pacientes consultar turnos y agendar citas médicas en sedes HIC e ICV, con auto-aprobación para medicina general (RN-02) y revisión administrativa para especialidades (RN-03).

## Actores
- USER (Paciente)
- PROFESSIONAL (Médico)
- ADMIN (Administrador)

## Alcance
- Catálogos de sedes, especialidades y estados de cita.
- Gestión de profesionales y publicación de bloques de disponibilidad (30 min slots).
- Consulta de horarios libres con soporte de duración de 30/60 min (RN-05).
- Agendamiento de citas generales (`APPROVED`) y especializadas (`REQUESTED`).
- Prevención de doble reserva (RN-01).
- Cancelación por el paciente y liberación de franjas (RN-09).
- Bandeja administrativa de aprobación y rechazo con motivo obligatorio (RN-04).
- Agenda médica del profesional y registro de atención (`COMPLETED`, `NO_SHOW`).

## Historias de usuario
- [[HU-004-consultar-disponibilidad]]
- [[HU-005-agendar-cita]]
- [[HU-006-gestion-administrativa-y-medica]]
