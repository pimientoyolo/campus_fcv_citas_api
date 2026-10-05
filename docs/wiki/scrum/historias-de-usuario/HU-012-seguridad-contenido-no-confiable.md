# HU-012 — Seguridad Frente a Contenido No Confiable

## Estado
- **Estado**: Completada
- **Sprint**: S5
- **Aprobado por**: Product Owner / FCV

## Descripción
Como equipo de seguridad y arquitectura, deseo que el sistema y los agentes autónomos procesen todo contenido externo (issues, notas, respuestas MCP y motivos de cancelación) como datos delimitados y no ejecutables, para mitigar ataques de inyección indirecta de prompt.

## Criterios de Aceptación
1. **Validación y Desinfección**: Toda entrada textual externa debe validar tamaño máximo y escapar caracteres de control.
2. **Delimitación de Contexto**: El contenido no confiable se etiqueta explícitamente y se aísla de las instrucciones directivas.
3. **Mínimo Privilegio**: Las credenciales de integración externas poseen scopes estrictamente limitados.
4. **Documentación de Riesgos Residuales**: Declaración formal de límites de seguridad en la wiki del proyecto.

## Evidencia de Cumplimiento
- **Documentación Wiki**: `docs/wiki/llm-wiki/wiki/seguridad-contenido-no-confiable.md`.
- **Demostración de Inyección Neutralizada**: `docs/evidence/s5/untrusted-content-demo.json`.
