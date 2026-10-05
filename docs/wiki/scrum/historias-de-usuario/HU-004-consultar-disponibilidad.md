---
id: HU-004
tipo: historia-de-usuario
titulo: Consultar disponibilidad de citas
estado: Completada
epica: "[[EP-002-gestion-y-agendamiento-de-citas]]"
prioridad: Alta
puntos-estimados: 5
sprint_sugerido: S3
dependencias:
  - "[[HU-001-registrar-usuario]]"
  - "[[HU-002-gestionar-sesion]]"
relacionadas:
  - "[[HU-005-agendar-cita]]"
  - "[[HU-006-gestion-administrativa-y-medica]]"
---

# HU-004 — Consultar disponibilidad de citas

**COMO** paciente autenticado (USER), **QUIERO** filtrar turnos por sede hospitalaria, especialidad médica, profesional y fecha, **PARA** visualizar horarios disponibles reales y seleccionar el más conveniente.

## Contexto y alcance
PRD RF-05, RF-07, RF-08, RF-09. La búsqueda de disponibilidad consulta turnos generados a partir de los bloques de disponibilidad de los profesionales activos en sedes asignadas. Incluye validación de slots contiguos según la duración de la especialidad (30 o 60 min).

## Reglas de negocio
- RN-01: Exclusividad de slot (solo slots donde `appointment_id IS NULL`).
- RN-05: Duración según especialidad (30 min = 1 slot; 60 min = 2 slots contiguos en el mismo bloque horario).
- RN-06: No se permite consultar ni mostrar franjas horarias pasadas en relación a la fecha y hora actual.
- RN-07: Sede válida asignada al profesional.

## Tareas
- [x] T-01 (Medio): Implementar puertos y adaptadores JPA para bloques (`availability_blocks`) y slots (`professional_slots`).
- [x] T-02 (Medio): Implementar algoritmo de búsqueda en `SchedulingService.searchAvailableSlots` agrupando slots contiguos según duración de especialidad.
- [x] T-03 (Bajo): Endpoint REST `GET /api/availability/search` y pruebas unitarias/integración con reloj determinista (`Clock`).
- [x] T-04 (Medio): Componentes UI de selección de sede, especialidad, médico, selector de fecha y grilla de horarios.

## Criterios de aceptación
- CA-01: El sistema permite filtrar por sede (HIC/ICV), especialidad y opcionalmente profesional y fecha.
- CA-02: Especialidades de 30 minutos requieren 1 slot libre; especialidades de 60 minutos exigen 2 slots consecutivos dentro del mismo bloque.
- CA-03: No se devuelven turnos en el pasado ni slots ocupados por citas previas.
- CA-04: La interfaz web presenta selector de sede, especialidad, profesional y grilla de slots interactiva con estado claro.

## Definition of Done
- [x] CA-01 a CA-04 verificados con tests automatizados en backend y build en frontend.
- [x] Migraciones Flyway V2 y V3 aplicadas y validadas con relaciones foráneas e índices.
- [x] Pruebas de integración de consulta ejecutadas con reloj determinista pasando al 100%.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AvailabilityController.java#search`, `SchedulingFacade.java#searchAvailableSlots` | Permite consultar con query params `locationId`, `specialtyId`, `professionalId`, `date` |
| CA-02 | Cumple | `SchedulingService.java#searchAvailableSlots`, `SchedulingIntegrationTest.java#searchAvailabilityShowsFreeSlots` | Agrupa slots según duración (30 min -> 1 slot, 60 min -> 2 slots consecutivos) |
| CA-03 | Cumple | `ProfessionalSlotJpaRepository.java#findAvailableSlots`, `ProfessionalSlot.java#isAvailable` | Filtro `appointmentId IS NULL` y validación de fecha/hora vs `Clock` |
| CA-04 | Cumple | `citas-web/src/pages/BookingPage.tsx`, `npm run build` PASS | Grilla responsiva que renderiza botones de horarios habilitados |
| DoD | Cumple | `mvn test` 17/17 PASS, `docker compose exec citas-web-dev npm run build` PASS | Verificación de extremo a extremo sin errores de compilación ni runtime |

## Historial
- 2026-09-20: Especificación y diseño de arquitectura hexagonal de disponibilidad.
- 2026-10-04: Aprobación del usuario y cierre con evidencia técnica (Completada).
