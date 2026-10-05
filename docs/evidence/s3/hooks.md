# Evidencia de Calidad y Git Hooks — Sesión S3

## 1. Verificación Red → Green: Prevención de Doble Reserva (RN-01)
- **Defecto Inicial**: `assignSlots` actualizaba slots incondicionalmente sin verificar si `appointment_id` ya estaba ocupado, permitiendo que peticiones concurrentes se sobreescribieran.
- **Prueba Roja (Red)**: Se ejecutó `SchedulingConcurrencyTest` simulando dos peticiones concurrentes con `CyclicBarrier` disparadas exactamente al mismo milisegundo por pacientes distintos sobre el mismo turno.
- **Fix Mínimo Implementado**:
  * Cláusula atómica en base de datos: `UPDATE ProfessionalSlotEntity s SET s.appointmentId = :appointmentId WHERE s.id IN :slotIds AND s.appointmentId IS NULL`.
  * Verificación en capa de servicio (`SchedulingService`): Si el número de filas actualizadas es menor que los slots requeridos, se lanza inmediatamente `409 CONFLICT`.
- **Prueba Verde (Green)**: Se ejecutó nuevamente `SchedulingConcurrencyTest`.
  * Hilo 1: Recibió `201 CREATED` con estado `APPROVED`.
  * Hilo 2: Recibió `409 CONFLICT` ("Uno o más turnos seleccionados ya fueron tomados simultáneamente").
  * Aserción `assertThat(successCount.get()).isEqualTo(1)` y `assertThat(conflictCount.get()).isEqualTo(1)` superada exitosamente.
- **Log Estructurado del Loop**: Registrado en `citas-api/docs/evidence/s4/loops/LOOP-01.json`.

---

## 2. Red que dice "No": Git Hooks Pre-Commit
- **Ubicación**: `.githooks/pre-commit` en repositorios raíz, `citas-api` y `citas-web`.
- **Script de Instalación**: `scripts/install-hooks.sh` (`git config core.hooksPath .githooks`).

### Demostración 1 — Bloqueo de Secreto Ficticio:
- Se probó simular un commit conteniendo una clave secreta privada en texto plano en un archivo de configuración.
- **Salida del Hook**:
  ```text
  🔍 [pre-commit] Ejecutando validaciones de seguridad y calidad...
  ❌ [pre-commit] Error: Se detectaron posibles credenciales o secretos en el código staged:
  +[VARIABLE_DE_ENTORNO]="[CLAVE_DETECTADA_POR_EL_HOOK]"
  Por favor elimina los secretos del código antes de hacer commit.
  ```
- **Resultado**: El commit fue interceptado y abortado por la red de calidad.

### Demostración 2 — Desbloqueo y Commit Permitido:
- Una vez removido el secreto ficticio y verificados los archivos en stage, el hook ejecuta:
  ```text
  🔍 [pre-commit] Ejecutando validaciones de seguridad y calidad...
  ✅ [pre-commit] Filtros de seguridad pasados con éxito.
  ```
- El commit progresa normalmente hacia la rama `develop`.
