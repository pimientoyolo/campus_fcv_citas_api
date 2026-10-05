---
id: HU-006
tipo: historia-de-usuario
titulo: Gestión administrativa y agenda del profesional
estado: Completada
epica: "[[EP-002-gestion-y-agendamiento-de-citas]]"
prioridad: Alta
puntos-estimados: 8
sprint_sugerido: S3
dependencias:
  - "[[HU-005-agendar-cita]]"
---

# HU-006 — Gestión administrativa y agenda del profesional

**COMO** Administrador (ADMIN) o Profesional Médico (PROFESSIONAL), **QUIERO** gestionar la disponibilidad horaria, revisar solicitudes especializadas pendientes y registrar la atención médica de los pacientes, **PARA** garantizar la correcta operación clínica y la trazabilidad de cada cita hospitalaria.

## Contexto y alcance
PRD RF-07, RF-08, RF-15, RF-16, RF-17, RF-18, RN-04, RN-07, RN-09. Cubre el panel de control operativo: publicación y eliminación de bloques de disponibilidad por los médicos o admin, bandeja de aprobación/rechazo de citas especializadas por el administrador con motivo obligatorio de rechazo, consulta de agenda diaria para médicos y marcado del resultado de la consulta (`COMPLETED` o `NO_SHOW`) con validación temporal estricta para evitar cierres prematuros.

## Reglas de negocio
- RN-04: El rechazo administrativo de una solicitud de cita requiere obligatoriamente un motivo de rechazo no vacío (`rejection_reason`).
- RN-07: Solo se pueden publicar bloques en sedes donde el profesional esté formalmente asignado.
- RN-09: El rechazo de una cita libera de inmediato los slots para que otros pacientes puedan agendarlos.
- RF-17: El cierre de atención (`COMPLETED` o `NO_SHOW`) solo se permite si el horario programado ya inició (`scheduledStartAt <= now`).

## Tareas
- [x] T-01 (Medio): Implementar publicación y borrado de bloques en `ProfessionalController.java` y discretización a slots en `SchedulingService.java`.
- [x] T-02 (Medio): Implementar endpoints administrativos de aprobación y rechazo en `AppointmentController.java` con validación de motivo y liberación de slots.
- [x] T-03 (Medio): Implementar endpoints de profesional médico para agenda y registro de atención (`complete`, `noShow`) con guardia temporal.
- [x] T-04 (Medio): Implementar vistas web para gestión de bloques, bandeja administrativa de solicitudes y agenda clínica del médico.

## Criterios de aceptación
- CA-01: El administrador visualiza en su bandeja las citas en estado `REQUESTED` con todos los detalles clínicos.
- CA-02: El administrador puede aprobar una cita solicitada (`APPROVED`) o rechazarla (`REJECTED`) proporcionando obligatoriamente una justificación; el rechazo libera los slots.
- CA-03: El médico puede consultar sus citas asignadas para una fecha seleccionada.
- CA-04: El médico puede registrar el cierre de atención como `COMPLETED` o `NO_SHOW`, impidiendo el cierre si la cita aún no ha iniciado.
- CA-05: El médico o administrador puede publicar bloques de disponibilidad válidos que se discretizan en slots de 30 minutos sin solapamientos.

## Definition of Done
- [x] CA-01 a CA-05 verificados con tests automatizados en backend y frontend.
- [x] RBAC verificado: solo ADMIN accede a aprobación/rechazo; solo PROFESSIONAL y ADMIN asignado gestionan bloques y cierres de consulta.
- [x] Transiciones de estado auditadas en `appointment_status_history`.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AppointmentController.java#listPending`, `AdminAppointmentsPage.tsx` | Lista citas con filtro `status = REQUESTED` |
| CA-02 | Cumple | `AppointmentController.java#approveAppointment`, `AppointmentController.java#rejectAppointment` | Rechazo valida `reason != null && !blank`, pasa a estado 4 (`REJECTED`) y libera slots |
| CA-03 | Cumple | `AppointmentController.java#professionalAppointments`, `DoctorAgendaPage.tsx` | Lista citas del profesional autenticado por fecha y sede |
| CA-04 | Cumple | `SchedulingService.java#completeAppointment`, `SchedulingService.java#noShowAppointment` | Valida `scheduledStartAt <= now` y actualiza estado a `COMPLETED` o `NO_SHOW` |
| CA-05 | Cumple | `SchedulingService.java#createBlock`, `SchedulingServiceTest.java#createBlockDiscretizesInto30MinuteSlots` | Genera slots de 30 mins y valida asignación de sede |
| DoD | Cumple | `mvn test` 17/17 PASS, compilación limpia en Docker | Cobertura integral de endpoints y reglas clínicas |

## Historial
- 2026-09-24: Especificación de flujos administrativos y operativos del personal de salud.
- 2026-10-04: Implementación de guardia de cierre temporal y verificación completa (Completada).
