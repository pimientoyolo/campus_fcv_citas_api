# Plan Scrum — FCV Citas

## Incrementos
- **S2 (Acceso de usuarios)**:
  - Épica: [[EP-001-acceso-de-usuarios]].
  - Historias: [[HU-001-registrar-usuario]], [[HU-002-gestionar-sesion]], [[HU-003-interfaz-de-acceso]].
  - Estado: Implementado.
- **S3 (Gestión y agendamiento de citas)**:
  - Épica: [[EP-002-gestion-y-agendamiento-de-citas]].
  - Historias: [[HU-004-consultar-disponibilidad]], [[HU-005-agendar-cita]], [[HU-006-gestion-administrativa-y-medica]].
  - Estado: Implementado.

## Stack y Tecnologías
- Backend: Java 21, Spring Boot 3.5.x, Maven, MySQL 8.4, JPA, Flyway, Spring Security JWT.
- Frontend: React 19, TypeScript, Vite, Vanilla CSS.
- Arquitectura: Hexagonal en citas-api; REST directo sin BFF en citas-web.

