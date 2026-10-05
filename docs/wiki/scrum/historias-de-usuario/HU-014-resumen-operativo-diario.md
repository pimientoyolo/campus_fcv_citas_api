# HU-014 — Generación de Resumen Operativo Diario

## Estado
- **Estado**: Completada
- **Sprint**: S6
- **Aprobado por**: Product Owner / FCV

## Descripción
Como coordinador de operaciones hospitalarias, deseo recibir un informe consolidado diario con la cantidad de citas atendidas, canceladas e inasistencias por sede, para evaluar la ocupación y oportunidad de la atención médica.

## Criterios de Aceptación
1. **Endpoint de Resumen Operativo**: `GET /api/integrations/reports/daily-summary` retorna métricas agregadas por estado y por sede para una fecha dada.
2. **Generación Automática**: El flujo n8n `WF-003-daily-operational-summary.json` se dispara a las 20:00 diariamente.
3. **Formato Claro**: El reporte se estructura en una tabla HTML enviada al equipo de operaciones.

## Evidencia de Cumplimiento
- **Código Backend**: `IntegrationController.java` (`/reports/daily-summary`), `SchedulingService.java`.
- **Workflow n8n**: `automations/n8n/WF-003-daily-operational-summary.json`.
- **Prueba Automatizada**: `co.fcv.citas.IntegrationEndpointsTest#fullIntegrationsWorkflow`.
