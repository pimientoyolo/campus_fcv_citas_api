---
id: HU-001
tipo: historia-de-usuario
titulo: Registrar usuario
estado: Completada
epica: "[[EP-001-acceso-de-usuarios]]"
esfuerzo: Medio
sprint_sugerido: S2
dependencias: []
relacionadas:
  - "[[HU-002-gestionar-sesion]]"
  - "[[HU-003-interfaz-de-acceso]]"
---

# HU-001 — Registrar usuario

**COMO** visitante ficticio, **QUIERO** registrar mi cuenta, **PARA** acceder como paciente al sistema.

## Contexto y alcance
PRD RF-01. Capturar nombres, apellidos, tipo/número de documento, email, teléfono y contraseña. Persistencia MySQL y migración inicial. No incluye afiliación ni recuperación de contraseña.

## Reglas de negocio
Email y documento únicos. Normalizar email; almacenar exclusivamente hash de contraseña. Asignar USER desde el servidor, sin admitir escalamiento por datos del cliente.

## Dependencias y relaciones
Épica [[EP-001-acceso-de-usuarios]]. Habilita [[HU-002-gestionar-sesion]] y [[HU-003-interfaz-de-acceso]].

## Esfuerzo
Medio: coordina validación, caso de uso y persistencia.

## Tareas
- [x] T-01 (Medio): modelar usuarios, roles y migración Flyway en 3FN.
- [x] T-02 (Medio): implementar registro, validación y hash adaptativo.
- [x] T-03 (Medio): verificar persistencia, duplicados y rol del usuario registrado.

## Criterios de aceptación
- CA-01: con datos válidos, el registro responde 201 con identificador y rol USER, sin contraseña ni hash.
- CA-02: email repetido, incluso con diferencias de mayúsculas, o documento repetido responde 409 sin crear otra cuenta.
- CA-03: campos obligatorios vacíos, email inválido o contraseña fuera de límites documentados responde 400.
- CA-04: una petición que intente asignar ADMIN no crea una cuenta privilegiada.

## Definition of Done
- [x] CA-01 a CA-04 verificados con evidencia técnica.
- [x] Migración inicial ejecutada en MySQL (V1__identity_and_sessions.sql); esquema 3FN validado.
- [x] Contraseña persistida como hash BCrypt; no aparece en respuestas ni logs.
- [x] Contrato y trazabilidad actualizados.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AuthService.java#register`, `AuthIntegrationTest.java#registrationPersistsHashAndOnlyUserRole` | Responde 201 con UserResponse (id, email, roles=["USER"]) sin hash |
| CA-02 | Cumple | `UserPersistenceAdapter.java#create`, `smoke-auth.mjs` paso 4-5 | Lanza 409 CONFLICT ante email en mayúsculas o documento repetido |
| CA-03 | Cumple | `RegisterRequest.java`, `AuthIntegrationTest.java#validationAndPrivilegeEscalationAreRejected` | `@NotBlank`, `@Email`, `@Size` validan y devuelven 400 |
| CA-04 | Cumple | `AuthService.java#register`, `AuthIntegrationTest.java#validationAndPrivilegeEscalationAreRejected` | Rol asignado exclusivamente por el servidor (`Set.of("USER")`) |
| DoD | Cumple | `V1__identity_and_sessions.sql`, `BCryptPasswordEncoder` en `SecurityConfiguration.java` | Verificado en H2 (tests) y MySQL (smoke-auth 18/18 PASS) |

## Historial
- 2026-09-16: Redacción inicial de la propuesta S2.
- 2026-10-04: Aprobación explícita del usuario y cierre con evidencia técnica (Completada).
