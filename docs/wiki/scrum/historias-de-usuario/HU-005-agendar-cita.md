---
id: HU-005
tipo: historia-de-usuario
titulo: Agendar y gestionar citas del paciente
estado: Completada
epica: "[[EP-002-gestion-y-agendamiento-de-citas]]"
prioridad: Alta
puntos-estimados: 8
sprint_sugerido: S3
dependencias:
  - "[[HU-004-consultar-disponibilidad]]"
relacionadas:
  - "[[HU-006-gestion-administrativa-y-medica]]"
---

# HU-005 — Agendar y gestionar citas del paciente

**COMO** paciente autenticado (USER), **QUIERO** confirmar la reserva de un turno horario y consultar el estado de mis citas, **PARA** asegurar mi atención en la sede y horario escogidos con garantías contra sobreventa.

## Contexto y alcance
PRD RF-10, RF-11, RF-12, RF-13, RF-14, RN-01, RN-02, RN-03, RN-09. Abarca el flujo transaccional de agendamiento: validación de disponibilidad atómica, asignación condicional de estado (General -> `APPROVED`, Especializada -> `REQUESTED`), protección estricta contra reserva concurrente mediante asignación atómica de slots (`UPDATE ... WHERE appointment_id IS NULL`), consulta de citas del usuario autenticado y cancelación antes del inicio del turno.

## Reglas de negocio
- RN-01: Exclusividad y control de concurrencia en slots (bloqueo atómico a nivel BD).
- RN-02: Citas de Medicina General quedan auto-aprobadas (`APPROVED`) inmediatamente.
- RN-03: Citas de Especialidades quedan en estado solicitado (`REQUESTED`) con slots retenidos.
- RN-08: Validación de citas en el pasado rechazada con 400 Bad Request.
- RN-09: Cancelación o rechazo libera inmediatamente los slots asociados (`appointment_id = NULL`).

## Tareas
- [x] T-01 (Alto): Implementar `SchedulingService.bookAppointment` con asignación atómica de slots y resolución de estado según tipo de especialidad.
- [x] T-02 (Alto): Implementar prueba de concurrencia multihilo (`SchedulingConcurrencyTest.java`) usando `CyclicBarrier` para validar RN-01 (1 éxito 201, 1 conflicto 409).
- [x] T-03 (Medio): Implementar endpoints de cancelación (`POST /api/appointments/{id}/cancel`) y listado del paciente (`GET /api/appointments/my`).
- [x] T-04 (Medio): Interfaz web en `citas-web` con confirmación de reserva y listado "Mis Citas" con chips de estado.

## Criterios de aceptación
- CA-01: Citas de Medicina General (`is_general = true`) quedan en estado `APPROVED` sin requerir intervención administrativa.
- CA-02: Citas de especialidades (`requires_admin_approval = true`) se crean en estado `REQUESTED` reteniendo sus slots contiguos.
- CA-03: Ante dos solicitudes concurrentes para el mismo turno, exactamente una petición obtiene 201 Created y la otra recibe 409 Conflict.
- CA-04: El paciente puede ver el listado de sus citas con sede, médico, especialidad, fecha, hora y chip de estado.
- CA-05: El paciente puede cancelar una cita futura, lo que pasa la cita a `CANCELLED` y libera los slots correspondientes.

## Definition of Done
- [x] CA-01 a CA-05 verificados con tests automatizados unitarios, de integración y de estrés concurrente.
- [x] Prueba de concurrencia multihilo ejecutada y aprobada (evidencia en `SchedulingConcurrencyTest.java` y `LOOP-01.json`).
- [x] Trazabilidad histórica registrada en `appointment_status_history` ante cada transición de estado.
- [x] Cero fugas de información personal en endpoints de citas e historial.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `SchedulingService.java#bookAppointment`, `SchedulingIntegrationTest.java#bookGeneralMedicineAppointmentIsAutoApproved` | Medicina General auto-aprueba a estado 2 (`APPROVED`) |
| CA-02 | Cumple | `SchedulingService.java#bookAppointment`, `SchedulingIntegrationTest.java#bookSpecializedAppointmentRequiresAdminApproval` | Especializada pasa a estado 1 (`REQUESTED`) con slots retenidos |
| CA-03 | Cumple | `SchedulingConcurrencyTest.java#concurrentBookingSameSlotResultsInOneWinnerAndOneConflict` | Hilos concurrentes con `CyclicBarrier`: 1 HTTP 201, 1 HTTP 409 (RN-01 verificado) |
| CA-04 | Cumple | `AppointmentController.java#myAppointments`, `MyAppointmentsPage.tsx` | Filtra por `patientUserId` del JWT autenticado |
| CA-05 | Cumple | `AppointmentController.java#cancel`, `SchedulingIntegrationTest.java#cancelAppointmentFreesSlots` | Cita pasa a `CANCELLED` y slots quedan libres para nuevo agendamiento |
| DoD | Cumple | `mvn test` 17/17 PASS, evidencia `LOOP-01.json` | Flujo transaccional robusto y verificado en BD real MySQL/H2 |

## Historial
- 2026-09-22: Diseño del modelo relacional de agendamiento y máquina de estados de cita.
- 2026-10-04: Implementación de mitigación anti-doble reserva y verificación con `SchedulingConcurrencyTest` (Completada).
