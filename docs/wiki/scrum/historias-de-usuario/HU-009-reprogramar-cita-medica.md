---
id: HU-009
tipo: historia-de-usuario
titulo: Reprogramar cita médica
estado: Completada
epica: "[[EP-002-gestion-y-agendamiento-de-citas]]"
prioridad: Alta
puntos-estimados: 8
sprint_sugerido: S4
dependencias:
  - "[[HU-005-agendar-cita]]"
---

# HU-009 — Reprogramar cita médica

**COMO** paciente o administrador, **QUIERO** solicitar la reprogramación de una cita previamente agendada indicando un motivo y nueva fecha, **PARA** ajustar la atención médica a imprevistos sin perder el seguimiento clínico.

## Contexto y alcance
PRD RF-15, RF-18. El paciente puede solicitar reprogramar una cita activa no-terminal a un nuevo horario disponible en el futuro. Si la reprogramación la ejecuta el Administrador, se auto-aprueba reasignando turnos inmediatamente; si la solicita el paciente, queda en estado `PENDING` para revisión administrativa o auto-resolución, protegiendo los slots involucrados.

## Reglas de negocio
- No se puede reprogramar una cita en estado terminal (`CANCELLED`, `REJECTED`, `COMPLETED`, `NO_SHOW`).
- La nueva fecha de reprogramación debe ser estrictamente futura en relación al reloj del sistema.
- Los nuevos slots requeridos deben encontrarse libres y no superponerse.
- La aprobación administrativa libera los slots anteriores y asigna de forma atómica los nuevos slots a la cita existente.

## Tareas
- [x] T-01 (Medio): Migración Flyway `V6__reschedules.sql` creando `reschedule_statuses` y `appointment_reschedules`.
- [x] T-02 (Alto): Implementar puertos y casos de uso en `SchedulingPorts.Reschedules` y `SchedulingService` (`requestReschedule`, `approveReschedule`, `rejectReschedule`).
- [x] T-03 (Bajo): Endpoints REST en `AppointmentController` (`/api/appointments/{id}/reschedule`, `/api/admin/reschedules/**`).
- [x] T-04 (Medio): Prueba de integración completa en `S4FeaturesIntegrationTest#appointmentRescheduleFlow`.

## Criterios de aceptación
- CA-01: El paciente puede solicitar reprogramación para un horario disponible futuro, registrando el motivo.
- CA-02: La solicitud queda en estado `PENDING` si la efectúa el paciente.
- CA-03: El Administrador puede consultar las solicitudes de reprogramación pendientes y aprobarlas.
- CA-04: La aprobación actualiza el horario de la cita original, libera los slots antiguos y bloquea los nuevos slots.
- CA-05: El rechazo administrativo exige obligatoriamente un motivo de rechazo.

## Definition of Done
- [x] CA-01 a CA-05 verificados con `S4FeaturesIntegrationTest#appointmentRescheduleFlow` (PASS).
- [x] Reasignación atómica de slots verificada en base de datos.
- [x] Trazabilidad histórica registrada en `appointment_status_history`.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AppointmentController.java#reschedule` | Valida nueva fecha futura y disponibilidad de slots |
| CA-02 | Cumple | `SchedulingService.java#requestReschedule` | Crea registro con `statusId = 1` (`PENDING`) |
| CA-03 | Cumple | `AppointmentController.java#listPendingReschedules` | Filtra solicitudes en estado pendiente |
| CA-04 | Cumple | `SchedulingService.java#approveReschedule` | Libera slots viejos, asigna nuevos y actualiza `scheduledStartAt`/`scheduledEndAt` |
| CA-05 | Cumple | `AppointmentController.java#rejectReschedule` | Rechazo valida `rejectionReason` no vacío |
| DoD | Cumple | `S4FeaturesIntegrationTest` PASS | Ciclo de reprogramación validado |

## Historial
- 2026-10-01: Especificación del flujo de reprogramaciones clínicas.
- 2026-10-04: Implementación, verificación y cierre (Completada).
