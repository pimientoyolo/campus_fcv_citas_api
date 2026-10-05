---
id: HU-008
tipo: historia-de-usuario
titulo: Gestionar perfil y afiliación a EPS
estado: Completada
epica: "[[EP-001-acceso-de-usuarios]]"
prioridad: Media
puntos-estimados: 5
sprint_sugerido: S4
dependencias:
  - "[[HU-001-registrar-usuario]]"
  - "[[HU-002-gestionar-sesion]]"
---

# HU-008 — Gestionar perfil y afiliación a EPS

**COMO** paciente autenticado (USER), **QUIERO** consultar y actualizar mis datos de perfil y asociar mi EPS, plan y régimen de afiliación, **PARA** registrar mi cobertura médica ante la FCV y facilitar la facturación de servicios de salud.

## Contexto y alcance
PRD RF-04. El usuario autenticado consulta su información de perfil y teléfono, puede actualizar su número de contacto y nombres, y gestionar su historial de afiliaciones activas (EPS, Plan y Régimen Contributivo/Subsidiado/Particular). Se restringe la creación de duplicados activos idénticos.

## Reglas de negocio
- El paciente puede consultar y actualizar datos permitidos (teléfono, nombres).
- Se evita duplicar una afiliación con idéntica EPS, plan y régimen si ya se encuentra activa.
- La desafiliación o cambio desactiva la afiliación anterior (`active = false`).

## Tareas
- [x] T-01 (Medio): Migración Flyway `V5__affiliation_catalogs.sql` creando `regimens`, `eps_entities`, `eps_plans` y `user_affiliations`.
- [x] T-02 (Medio): Implementar puertos y repositorio en `SchedulingPorts.Affiliations`.
- [x] T-03 (Bajo): Endpoints REST en `UserController` (`/api/users/profile`, `/api/users/affiliations`).
- [x] T-04 (Bajo): Pruebas de integración automatizadas en `S4FeaturesIntegrationTest`.

## Criterios de aceptación
- CA-01: El usuario autenticado puede consultar su perfil con sus afiliaciones activas.
- CA-02: El usuario puede modificar su teléfono y datos de perfil permitidos.
- CA-03: El usuario puede registrar una nueva afiliación especificando EPS, plan y régimen.
- CA-04: Intentar registrar una afiliación idéntica ya activa responde 409 Conflict.
- CA-05: El usuario puede dar de baja una afiliación previa (204 No Content).

## Definition of Done
- [x] CA-01 a CA-05 verificados con `S4FeaturesIntegrationTest#profileAndAffiliationManagement` (PASS).
- [x] Constraints de unicidad validados en base de datos.
- [x] Datos personales protegidos bajo autenticación JWT.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `UserController.java#getProfile` | Retorna datos personales y arreglo de afiliaciones con nombres de EPS y plan |
| CA-02 | Cumple | `UserController.java#updateProfile`, `AuthService.java#updateProfile` | Actualiza teléfono y persiste cambios en `app_users` |
| CA-03 | Cumple | `UserController.java#createAffiliation`, `SchedulingService.java#createAffiliation` | Inserta nueva afiliación en `user_affiliations` con estado activo |
| CA-04 | Cumple | `SchedulingService.java#createAffiliation` | Detecta combinación repetida y lanza `SchedulingFailure.conflict` (409) |
| CA-05 | Cumple | `UserController.java#deactivateAffiliation` | Marca `active = false` respondiendo 204 No Content |
| DoD | Cumple | `S4FeaturesIntegrationTest` PASS | Cobertura integral verificada |

## Historial
- 2026-09-30: Diseño del modelo de afiliaciones hospitalarias FCV.
- 2026-10-04: Implementación, verificación y cierre (Completada).
