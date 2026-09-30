---
id: HU-005
tipo: historia-de-usuario
titulo: Agendar y gestionar citas del paciente
estado: Implementada
epica: "[[EP-002-gestion-y-agendamiento-de-citas]]"
prioridad: Alta
puntos-estimados: 8
dependencias:
  - "[[HU-004-consultar-disponibilidad]]"
---

# HU-005 — Agendar y gestionar citas del paciente

## Como
Paciente autenticado (USER),
## Quiero
Confirmar la reserva de un turno seleccionado y visualizar el estado de mis citas,
## Para
Asegurar mi atención médica en la fecha y sede acordada.

## Criterios de Aceptación
1. **CA-1 (Medicina General):** Si la cita es de Medicina General, se aprueba automáticamente en estado `APPROVED` sin intervención administrativa (RN-02).
2. **CA-2 (Especialidades):** Si la cita es especializada, se registra en estado `REQUESTED` y sus slots quedan retenidos para evitar colisiones (RN-03, RN-01).
3. **CA-3 (Anti-doble reserva):** Si dos usuarios intentan reservar el mismo slot simultáneamente, uno triunfa y el segundo recibe 409 CONFLICT (RN-01).
4. **CA-4 (Mis Citas):** El paciente puede listar sus citas con sede, médico, fecha, duración y estado con chip de color identificativo (RF-13).
5. **CA-5 (Cancelación):** El paciente puede cancelar una cita futura no terminal, liberando inmediatamente los slots (RF-14, RN-09).
