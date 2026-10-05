# HU-011 — Recordatorios de Citas Próximas vía n8n

## Estado
- **Estado**: Completada
- **Sprint**: S5
- **Aprobado por**: Product Owner / FCV

## Descripción
Como paciente con una cita aprobada, deseo recibir un recordatorio automático por correo electrónico 24 horas antes de mi consulta, para presentarme oportunamente en la sede correspondiente.

## Criterios de Aceptación
1. **Consulta de Citas Próximas**: El endpoint `GET /api/integrations/appointments/upcoming` debe retornar únicamente citas en estado `APPROVED` en el rango de tiempo solicitado.
2. **Seguridad del Endpoint**: La llamada requiere autenticación mediante el encabezado `X-Integration-Key`.
3. **Flujo Automatizado en n8n**: El workflow `WF-001-appointment-reminders.json` se ejecuta de forma periódica, procesa cada cita y envía el correo con la información de fecha, hora, médico y sede.
4. **Registro de Recordatorio**: Tras el envío, se invoca `POST /api/integrations/appointments/reminders` para registrar la trazabilidad del recordatorio.

## Evidencia de Cumplimiento
- **Código Backend**: `IntegrationController.java`, `IntegrationPersistenceAdapter.java`, `SchedulingService.java`.
- **Workflow n8n**: `automations/n8n/WF-001-appointment-reminders.json`.
- **Prueba Automatizada**: `co.fcv.citas.IntegrationEndpointsTest#fullIntegrationsWorkflow`.
