# Plan Scrum — FCV Citas

## Incrementos y Entregas
- **S2 (Acceso de usuarios)**:
  - Épica: [[EP-001-acceso-de-usuarios]].
  - Historias: [[HU-001-registrar-usuario]], [[HU-002-gestionar-sesion]], [[HU-003-interfaz-de-acceso]].
  - Estado: Completada.
- **S3 (Gestión y agendamiento de citas)**:
  - Épica: [[EP-002-gestion-y-agendamiento-de-citas]].
  - Historias: [[HU-004-consultar-disponibilidad]], [[HU-005-agendar-cita]], [[HU-006-gestion-administrativa-y-medica]].
  - Estado: Completada.
- **S4 (Ciclo de vida, perfiles y catálogos)**:
  - Historias: [[HU-007-recuperar-contrasena]], [[HU-008-gestionar-perfil-y-afiliacion]], [[HU-009-reprogramar-cita-medica]], [[HU-010-gestion-catalogos-configurables]].
  - Estado: Completada.
- **S5 (Agente conectado, n8n y seguridad de contenido no confiable)**:
  - Historias: [[HU-011-recordatorios-citas-n8n]], [[HU-012-seguridad-contenido-no-confiable]].
  - Estado: Completada.
- **S6 (Automatizaciones del agente y cierre operativo)**:
  - Historias: [[HU-013-notificaciones-cambio-estado-n8n]], [[HU-014-resumen-operativo-diario]].
  - Estado: Completada.

## Stack y Tecnologías
- Backend: Java 21, Spring Boot 3.5.x, Maven, MySQL 8.4, JPA, Flyway, Spring Security JWT.
- Frontend: React 19, TypeScript, Vite, Vanilla CSS.
- Automatizaciones: n8n (WF-001, WF-002, WF-003).
- Arquitectura: Hexagonal en citas-api; REST directo sin BFF en citas-web.
