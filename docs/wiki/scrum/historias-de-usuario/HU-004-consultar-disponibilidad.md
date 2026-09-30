---
id: HU-004
tipo: historia-de-usuario
titulo: Consultar disponibilidad de citas
estado: Implementada
epica: "[[EP-002-gestion-y-agendamiento-de-citas]]"
prioridad: Alta
puntos-estimados: 5
dependencias:
  - "[[HU-001-registrar-usuario]]"
  - "[[HU-002-gestionar-sesion]]"
---

# HU-004 — Consultar disponibilidad de citas

## Como
Paciente autenticado (USER),
## Quiero
Filtrar por sede hospitalaria, especialidad, profesional médico y fecha,
## Para
Visualizar los turnos horarios disponibles para agendar una cita.

## Criterios de Aceptación
1. **CA-1:** El sistema permite seleccionar sedes fijas (HIC, ICV) y especialidades (Medicina General, Cardiología, Pediatría, etc.).
2. **CA-2:** La consulta de disponibilidad valida la duración de la especialidad: 30 minutos requiere 1 slot libre; 60 minutos requiere 2 slots consecutivos en el mismo bloque (RN-05).
3. **CA-3:** No se muestran turnos en horarios pasados (RN-06) ni franjas ya ocupadas o retenidas por otra cita (RN-01).
4. **CA-4:** La interfaz web muestra una grilla interactiva con los turnos disponibles listos para selección.
