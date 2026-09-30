# Agente de citas-api

## Arquitectura Hexagonal y Stack
Java 21, Spring Boot 3.5.x, Maven, JPA, MySQL 8.4 y Flyway.
- `domain/`: Registros de usuario y sesión, **sin dependencias a Spring/JPA/HTTP**.
- `application/`: Casos de uso y puertos (representan dependencias hacia dentro/fuera), sin dependencias Spring/JPA.
- `adapter/persistence/`: Entidades y repositorios JPA (adaptador).
- `adapter/security/`: JWT, BCrypt, autorización y CORS.
- `adapter/web/`: DTO validados, controladores, errores. **Los controladores traducen HTTP; no concentran negocio**.

**Reglas Críticas de Arquitectura:**
- No acoples el backend a React/Angular.
- **No edites `citas-web` desde este agente.**
- No exponer entidades o hashes como respuestas HTTP. Registro público solo USER.
- Secretos solo por variables de entorno.
- No registrar tokens/passwords en logs.

## Comandos
Con Java 21/Maven: `mvn -B -ntp verify`.
Desde workspace Docker: `docker compose exec -T citas-api-dev mvn -B -ntp verify`.
Prueba HTTP contra MySQL: `node scripts/smoke-auth.mjs`, con API en localhost:8080 o `API_BASE_URL` configurada.

## Datos y documentación
- Cambios de esquema requieren nueva migración Flyway y **justificación**. No editar migraciones aplicadas.
- Tests de integración usan H2 aislado; la evidencia MySQL es separada.
- Desarrollo en `develop`; nunca reescribir historial para simular progreso.
- **Wiki:** No mantengas una LLM Wiki propia. La wiki global la mantiene el orquestador en `docs/wiki/llm-wiki/`.

## Modo de Trabajo (Ciclo)
1. Localiza la HU aprobada y su DoD (en `docs/wiki/scrum/README.md`).
2. Identifica reglas de negocio y contratos REST afectados.
3. Propón un plan de implementación antes de editar código.
4. Implementa el mínimo coherente.
5. Ejecuta pruebas relevantes (`mvn -B -ntp verify`).
6. Verifica arquitectura y DoD.
7. Resume evidencia y deja explícito lo no verificado.
