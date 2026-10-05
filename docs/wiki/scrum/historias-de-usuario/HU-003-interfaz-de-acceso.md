---
id: HU-003
tipo: historia-de-usuario
titulo: Interfaz de acceso
estado: Aprobada
epica: "[[EP-001-acceso-de-usuarios]]"
esfuerzo: Medio
sprint_sugerido: S2
dependencias:
  - "[[HU-001-registrar-usuario]]"
  - "[[HU-002-gestionar-sesion]]"
relacionadas: []
---

# HU-003 — Interfaz de acceso

**COMO** paciente, **QUIERO** usar formularios de registro e inicio de sesión, **PARA** acceder desde el navegador.

## Contexto y alcance
Primer frontend S2. Login, registro, confirmación de sesión e integración REST. React/TypeScript propuesto si no existe exportación. No incluye agenda ni recuperación de contraseña.

## Reglas de negocio
Mostrar errores sin revelar credenciales. Evitar doble envío. Etiquetas accesibles y diseño adaptable. URL de API configurable. Identificar el entorno como académico.

## Dependencias y relaciones
Épica [[EP-001-acceso-de-usuarios]]. Depende de [[HU-001-registrar-usuario]] y [[HU-002-gestionar-sesion]].

## Esfuerzo
Medio: interfaz, validación y comunicación REST.

## Tareas
- [x] T-01 (Medio): confirmar origen del diseño y framework, importar o crear interfaz según decisión.
- [x] T-02 (Medio): integrar formularios y estados de envío/error/éxito.
- [x] T-03 (Medio): verificar build, tipos y flujo en navegador.

## Criterios de aceptación
- CA-01: el frontend arranca y permite alternar entre login y registro mediante controles accesibles.
- CA-02: registro exitoso permite iniciar sesión; errores 400/409 se muestran sin perder los datos no sensibles del formulario.
- CA-03: login válido muestra identidad y logout; login inválido muestra error genérico.
- CA-04: una falla de red muestra un mensaje accionable y restablece la posibilidad de enviar.
- CA-05: formularios utilizables en móvil y escritorio, con etiquetas, foco visible y estado de carga.

## Definition of Done
- [x] CA verificados y build/typecheck correctos (`npm run build`, `npm run typecheck`).
- [x] URL configurable (`VITE_API_URL`) y contrato REST coherente con backend.
- [x] Evidencia visual y procedencia real del diseño documentadas en `s2-evidencia.md`.
- [x] Trazabilidad actualizada; aprobación explícita registrada el 2026-10-04.

## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `citas-web/src/main.tsx` | Controles de navegación y switch login/registro |
| CA-02 | Cumple | `citas-web/src/api.ts#register` | Validación y feedback de errores 400/409 |
| CA-03 | Cumple | `citas-web/src/main.tsx` | Identidad en sesión y botón de cerrar sesión |
| CA-04 | Cumple | `citas-web/src/api.ts` | Manejo de error de red y timeout configurable |
| CA-05 | Cumple | `citas-web/src/styles.css` | Diseño responsive medido en 375px y escritorio |
| DoD | Cumple | Build Vite exitoso y TypeScript validado | Transición documentada a modularización S3 |

## Historial
- 2026-09-16: Redacción inicial de la propuesta S2.
- 2026-10-04: Aprobación explícita del usuario en plan de cierre (Aprobada / En validación para modularización).
