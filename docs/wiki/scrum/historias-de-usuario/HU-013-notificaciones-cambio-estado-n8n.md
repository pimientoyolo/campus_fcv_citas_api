# HU-013 — Notificación de Cambio de Estado vía Webhook y n8n

## Estado
- **Estado**: Completada
- **Sprint**: S6
- **Aprobado por**: Product Owner / FCV

## Descripción
Como paciente, deseo ser notificado inmediatamente cuando el estado de mi cita o solicitud de reprogramación cambie (aprobada, rechazada o cancelada), para mantenerme informado sin necesidad de consultar manualmente el portal.

## Criterios de Aceptación
1. **Emisión de Eventos Outbox**: Toda transición de estado de una cita emite un registro en `notification_events` con su tipo (`APPOINTMENT_APPROVED`, `APPOINTMENT_REJECTED`, `APPOINTMENT_CANCELLED`, `RESCHEDULE_APPROVED`, `RESCHEDULE_REJECTED`).
2. **Consumo y Despacho**: El flujo n8n `WF-002-status-notifications.json` recibe o consulta los eventos pendientes y ramifica según el tipo de novedad.
3. **Confirmación Determinista**: El webhook confirma el procesamiento exitoso invocando `POST /api/integrations/events/{id}/ack`.

## Evidencia de Cumplimiento
- **Código Backend**: `SchedulingService.java` (emisión de eventos), `IntegrationController.java` (`/events/pending`, `/events/{id}/ack`).
- **Workflow n8n**: `automations/n8n/WF-002-status-notifications.json`.
- **Prueba Automatizada**: `co.fcv.citas.IntegrationEndpointsTest#fullIntegrationsWorkflow`.
