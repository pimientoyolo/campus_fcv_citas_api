---
id: HU-010
tipo: historia-de-usuario
titulo: Gestión de catálogos configurables por ADMIN
estado: Completada
epica: "[[EP-002-gestion-y-agendamiento-de-citas]]"
prioridad: Media
puntos-estimados: 5
sprint_sugerido: S4
dependencias:
  - "[[HU-006-gestion-administrativa-y-medica]]"
---

# HU-010 — Gestión de catálogos configurables por ADMIN

**COMO** Administrador del sistema (ADMIN), **QUIERO** gestionar los catálogos de EPS, planes de salud y especialidades médicas mediante operaciones CRUD y activación/desactivación, **PARA** mantener actualizada la oferta asistencial y los convenios de la Fundación Cardiovascular.

## Contexto y alcance
PRD RF-05, RF-06. Los catálogos fijos (roles, estados de cita, estados de reprogramación, regímenes y sedes) son de solo lectura y precargados. Los catálogos configurables (EPS, planes de EPS y especialidades médicas) son administrados por el rol ADMIN. Para preservar la integridad referencial con transacciones históricas, se prohíbe el borrado físico y se implementa borrado lógico mediante flags de activación (`active: false`).

## Reglas de negocio
- Solo usuarios con rol `ADMIN` pueden crear, actualizar o desactivar entidades en los catálogos configurables.
- No se permite borrado físico; se utiliza desactivación (`active = false`).
- Códigos únicos para EPS, planes y especialidades.

## Tareas
- [x] T-01 (Medio): Migraciones Flyway `V2` y `V5` con tablas `eps_entities`, `eps_plans` y `specialties`.
- [x] T-02 (Medio): Implementar operaciones en `SchedulingPorts.Catalogs` y `CatalogPersistenceAdapter`.
- [x] T-03 (Bajo): Endpoints administrativos en `AdminCatalogController` (`/api/admin/catalogs/**`).
- [x] T-04 (Bajo): Pruebas de integración automatizadas en `S4FeaturesIntegrationTest#adminCatalogCrud`.

## Criterios de aceptación
- CA-01: El Administrador puede crear nuevas EPS y actualizar sus nombres o códigos.
- CA-02: El Administrador puede crear y actualizar planes asociados a una EPS.
- CA-03: El Administrador puede crear nuevas especialidades médicas definiendo su duración (30/60 min) y si requieren aprobación.
- CA-04: El Administrador puede activar o desactivar EPS, planes y especialidades mediante peticiones PATCH.
- CA-05: Peticiones no privilegiadas reciben 403 Forbidden.

## Definition of Done
- [x] CA-01 a CA-05 verificados con `S4FeaturesIntegrationTest#adminCatalogCrud` (PASS).
- [x] RBAC verificado (solo ADMIN).
- [x] Borrado lógico verificado sin romper claves foráneas.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AdminCatalogController.java#createEps`, `updateEps` | Crea y actualiza registros en `eps_entities` |
| CA-02 | Cumple | `AdminCatalogController.java#createPlan` | Asocia plan a `eps_id` garantizando integridad |
| CA-03 | Cumple | `AdminCatalogController.java#createSpecialty` | Persiste duración, flags `is_general` y `requires_admin_approval` |
| CA-04 | Cumple | `AdminCatalogController.java#toggleEpsActive`, `toggleSpecialtyActive` | Aplica soft-delete cambiando `active = false` |
| CA-05 | Cumple | `SecurityConfiguration.java` (`hasRole("ADMIN")`) | Protege endpoints administrativos |
| DoD | Cumple | `S4FeaturesIntegrationTest` PASS | Operaciones CRUD completas y auditables |

## Historial
- 2026-10-02: Especificación de catálogos configurables hospitalarios.
- 2026-10-04: Implementación, verificación y cierre (Completada).
