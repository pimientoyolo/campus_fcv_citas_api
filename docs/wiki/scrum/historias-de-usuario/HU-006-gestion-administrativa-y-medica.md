---
id: HU-006
tipo: historia-de-usuario
titulo: Gestión administrativa y agenda del profesional
estado: Implementada
epica: "[[EP-002-gestion-y-agendamiento-de-citas]]"
prioridad: Alta
puntos-estimados: 8
dependencias:
  - "[[HU-005-agendar-cita]]"
---

# HU-006 — Gestión administrativa y agenda del profesional

## Como
Administrador (ADMIN) o Profesional Médico (PROFESSIONAL),
## Quiero
Revisar solicitudes de citas, gestionar aprobaciones/rechazos y registrar la atención médica,
## Para
Controlar la operación hospitalaria y asegurar la trazabilidad de los pacientes.

## Criterios de Aceptación
1. **CA-1 (Bandeja Admin):** El ADMIN visualiza citas en estado `REQUESTED` con detalle del paciente, médico, especialidad y horario (RF-18).
2. **CA-2 (Aprobación/Rechazo Admin):** ADMIN puede aprobar (pasa a `APPROVED`) o rechazar la cita. El rechazo exige obligatoriamente un motivo explicativo (RN-04) y libera los slots correspondientes (RN-09).
3. **CA-3 (Agenda del Médico):** El médico consulta sus citas `APPROVED` asignadas para una fecha seleccionada (RF-16).
4. **CA-4 (Cierre de Atención):** El médico puede marcar la cita como `COMPLETED` (atendida) o `NO_SHOW` (no asistió) (RF-17).
5. **CA-5 (Gestión de Bloques):** El profesional puede publicar nuevos bloques de disponibilidad por fecha y sede, los cuales se discretizan automáticamente en turnos de 30 minutos (RF-08).
