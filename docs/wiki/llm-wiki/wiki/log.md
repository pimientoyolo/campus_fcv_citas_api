# Registro cronológico

- 2026-09-16 · INGEST: requisitos S2, restricciones y normalización de identidad.
- 2026-09-16 · LEARN: repos de aplicación inicialmente vacíos; Git raíz previo preservado; Docker iniciado para trabajar con Java 21 y MySQL.
- 2026-09-16 · LEARN: base React creada localmente como propuesta sin procedencia Stitch/AI Studio. HU pendientes de aprobación explícita.
- 2026-09-16 · LEARN: corregida conexión mediante overlay al esquema existente vacío `citas_fcv_training`; conservado volumen MySQL. Claves JWT de desarrollo generadas en archivo ignorado sin alterar `.env`.
- 2026-09-16 · LINT/VERIFY: 6 pruebas de integración H2 y 18 comprobaciones HTTP MySQL correctas; build frontend correcto; flujo de navegador registro/login/error/logout comprobado. Aprobaciones y origen Stitch/AI Studio pendientes; ver s2-evidencia.md.
- 2026-09-18 · INGEST: Creación de la carpeta `raw/` y copiado de `PRD.md`, `RESTRICCIONES_TECNICAS.md` y `REQUISITOS_NORMALIZACION_3FN.md` como fuentes inmutables para el patrón LLM-Wiki. Actualización del índice global.
- 2026-09-30 · LEARN/IMPLEMENT (S3): Implementación integral del modelo y flujo de citas (RF-07 a RF-18, RN-01 a RN-09). Migración Flyway V2 con tablas 3FN de sedes, especialidades, profesionales, bloques, slots, citas e historial. Backend hexagonal en citas-api con servicios, adaptadores JPA, endpoints REST y pruebas de integración. Frontend citas-web extendido con panel completo de agendamiento, mis citas, bandeja admin y agenda médica.
- 2026-09-30 · SEED/TEST DATA (S3): Migración Flyway V3__seed_test_appointments_and_blocks.sql con 34 citas sintéticas en todos los estados (APPROVED, REQUESTED, CANCELLED, REJECTED, COMPLETED, NO_SHOW), 15 bloques y 120 slots para múltiples pacientes y médicos en sedes HIC e ICV. Persistencia garantizada en Docker mediante volumen nombrado de MySQL y migraciones automáticas idempotentes de Flyway.
- 2026-10-04 · LEARN/RESET (S2-S6): Reinicio controlado del historial Git en los tres repositorios (orquestador raíz, citas-api y citas-web) a un commit inicial limpio con autoría unificada del estudiante, eliminando colaboradores externos no reconocidos provenientes de plantillas previas. Recreación inmediata de la rama `develop` para trazabilidad de trabajo progresivo S2 a S6 y aprobación integral del plan de cierre documentada.

