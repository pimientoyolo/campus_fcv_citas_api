---
id: HU-007
tipo: historia-de-usuario
titulo: Recuperar y restablecer contraseña
estado: Completada
epica: "[[EP-001-acceso-de-usuarios]]"
prioridad: Alta
puntos-estimados: 5
sprint_sugerido: S4
dependencias:
  - "[[HU-001-registrar-usuario]]"
  - "[[HU-002-gestionar-sesion]]"
---

# HU-007 — Recuperar y restablecer contraseña

**COMO** paciente registrado (USER), **QUIERO** solicitar el restablecimiento de mi contraseña mediante un token temporal y seguro, **PARA** recuperar el acceso a mi cuenta en caso de olvido.

## Contexto y alcance
PRD RF-03. Flujo en dos pasos: solicitud mediante correo registrado (generación de token aleatorio con hash SHA-256 y vigencia de 1 hora) y confirmación con nueva contraseña (validación de complejidad 8 a 72 bytes, actualización de hash en BD, consumo de token e invalidación de todas las sesiones activas concurrentes).

## Reglas de negocio
- Token de un solo uso (`used_at IS NULL`).
- Expiración estricta de 60 minutos.
- Inserción de hash de token en base de datos para no exponer tokens en claro.
- Cambiar la contraseña revoca todas las sesiones previas del usuario.

## Tareas
- [x] T-01 (Medio): Migración Flyway `V4__password_reset.sql` con tabla `password_reset_tokens`.
- [x] T-02 (Medio): Implementar puertos y casos de uso en `AuthService` (`requestPasswordReset`, `confirmPasswordReset`).
- [x] T-03 (Bajo): Endpoints REST públicos `POST /api/auth/password-reset/request` y `/confirm`.
- [x] T-04 (Bajo): Pruebas de integración automatizadas en `S4FeaturesIntegrationTest`.

## Criterios de aceptación
- CA-01: Solicitar recuperación con un correo existente genera un token con 1 hora de validez y responde confirmación.
- CA-02: Confirmar el cambio con token válido y nueva clave actualiza la contraseña y marca el token como utilizado.
- CA-03: Tras el cambio de contraseña, el inicio de sesión con la clave antigua falla (401) y con la nueva clave es exitoso (200).
- CA-04: Intentar reusar un token ya consumido o expirado responde error 400 Bad Request.

## Definition of Done
- [x] CA-01 a CA-04 verificados con prueba automatizada `S4FeaturesIntegrationTest#passwordResetFlowWorksEndToEnd` (PASS).
- [x] Migración V4 aplicada en MySQL y H2.
- [x] Revocación atómica de sesiones activas del usuario verificada.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AuthController.java#requestReset`, `AuthService.java#requestPasswordReset` | Genera token criptográfico y persiste hash en `password_reset_tokens` |
| CA-02 | Cumple | `AuthController.java#confirmReset`, `AuthService.java#confirmPasswordReset` | Consume token (`used_at = now()`) y actualiza BCrypt hash |
| CA-03 | Cumple | `S4FeaturesIntegrationTest.java#passwordResetFlowWorksEndToEnd` | Login viejo falla con 401, login nuevo responde 200 con JWT |
| CA-04 | Cumple | `AuthService.java#confirmPasswordReset`, token `isValid(clock.instant())` | Rechaza tokens usados o vencidos con mensaje explicativo |
| DoD | Cumple | `mvn test` 21/21 PASS | Flujo probado y documentado |

## Historial
- 2026-09-28: Especificación del flujo de restablecimiento seguro.
- 2026-10-04: Implementación, pruebas y cierre (Completada).
