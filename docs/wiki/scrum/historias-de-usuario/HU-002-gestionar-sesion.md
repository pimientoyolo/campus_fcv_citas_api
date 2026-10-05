---
id: HU-002
tipo: historia-de-usuario
titulo: Gestionar sesión JWT
estado: Completada
epica: "[[EP-001-acceso-de-usuarios]]"
esfuerzo: Alto
sprint_sugerido: S2
dependencias:
  - "[[HU-001-registrar-usuario]]"
relacionadas:
  - "[[HU-003-interfaz-de-acceso]]"
---

# HU-002 — Gestionar sesión JWT

**COMO** usuario registrado, **QUIERO** iniciar, renovar y cerrar mi sesión, **PARA** acceder a mis datos con autenticación.

## Contexto y alcance
PRD RF-02. Login por email/contraseña, JWT access de corta duración, refresh, logout e identidad autenticada. No incluye recuperación de contraseña ni pantallas de administración.

## Reglas de negocio
Credenciales incorrectas producen un error genérico. Tokens separados por propósito y clave, con expiración. Refresh de un solo uso mediante rotación y revocación persistente. Logout revoca la sesión; no conservar tokens en logs.

## Dependencias y relaciones
Épica [[EP-001-acceso-de-usuarios]]. Depende de [[HU-001-registrar-usuario]]; consumida por [[HU-003-interfaz-de-acceso]].

## Esfuerzo
Alto: coordina seguridad, persistencia y concurrencia de renovación.

## Tareas
- [x] T-01 (Medio): definir contrato de autenticación y errores.
- [x] T-02 (Alto): implementar emisión/validación, rotación y revocación.
- [x] T-03 (Medio): restringir rutas y CORS al origen configurado.
- [x] T-04 (Alto): probar acceso inválido, propósito de token y renovación repetida.

## Criterios de aceptación
- CA-01: credenciales válidas retornan access/refresh JWT con expiración e identidad; inválidas responden 401 genérico.
- CA-02: la consulta de identidad sin token, con token manipulado, expirado o refresh en lugar de access responde 401.
- CA-03: refresh válido emite un nuevo par; el anterior no puede reutilizarse.
- CA-04: logout invalida los tokens de esa sesión; otras sesiones independientes conservan su validez.
- CA-05: el origen permitido puede consumir la API; un origen distinto no recibe autorización CORS.

## Definition of Done
- [x] Todos los CA verificados, incluyendo persistencia MySQL de sesiones (`auth_sessions`).
- [x] Secretos externos al repositorio en variables de entorno; access y refresh con claves diferentes.
- [x] Pruebas de autenticación y autorización pasan (6 tests en `AuthIntegrationTest`).
- [x] Contrato, documentación y enlaces actualizados en la wiki.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AuthService.java#login`, `AuthIntegrationTest.java#sessionLifecycleAndTokenSeparation` | Emite tokens JWT independientes y rechaza contraseñas inválidas con 401 genérico |
| CA-02 | Cumple | `SecurityConfiguration.java`, `AuthIntegrationTest.java#expiredAccessAndMissingAuthenticationAreRejected` | Endpoints protegidos devuelven 401 si falta o expira el token |
| CA-03 | Cumple | `AuthService.java#refresh`, `AuthIntegrationTest.java#concurrentRefreshHasExactlyOneWinner` | Rotación atómica de refresh token; intento repetido resulta en 401 |
| CA-04 | Cumple | `AuthService.java#logout`, `SessionPersistenceAdapter.java#revokeSession` | La sesión se marca `revoked = true` en base de datos MySQL |
| CA-05 | Cumple | `SecurityConfiguration.java#corsConfigurationSource`, `AuthIntegrationTest.java#corsOnlyAllowsConfiguredFrontend` | Origen no permitido recibe rechazo de CORS |
| DoD | Cumple | `smoke-auth.mjs` (18 comprobaciones PASS contra MySQL real) | Persistencia y seguridad verificadas end-to-end |

## Historial
- 2026-09-16: Redacción inicial de la propuesta S2.
- 2026-10-04: Aprobación explícita del usuario y cierre con evidencia técnica (Completada).
