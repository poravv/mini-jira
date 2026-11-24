# Contexto para agentes de IA (Claude Code / Codex)

## Qué es este proyecto

Mini-Jira: plataforma de gestión de incidencias con fines formativos para desarrolladores junior. Monorepo con monolito modular. La definición completa está en `docs/definicion-proyecto-colaborativo-dev-jr.md` y la arquitectura en `docs/ARCHITECTURE.md`.

## Stack

- **Backend** (`apps/backend`): Java 21, Spring Boot 3, Maven, PostgreSQL 16, Liquibase, Swagger. Puerto 8080.
- **Frontend** (`apps/frontend`): Angular con componentes standalone, nginx en Docker (puerto 4200 → 80), proxy de `/api/` al backend.
- **MongoDB**: presente en compose, reservado para el futuro módulo de auditoría. El backend NO lo usa todavía.
- **Autenticación**: JWT stateless en el módulo `auth` (`POST /api/auth/login`, HS256, secreto en `JWT_SECRET`, expiración `PT8H`). Todo endpoint exige `Authorization: Bearer <token>` salvo el login, `GET /api/weather` y Swagger; los roles se controlan con `@PreAuthorize`. Diagramas en `docs/ARCHITECTURE.md`.

## Contrato de la API

- Autenticación en `/api/auth`: `POST /api/auth/login` (`{identifier, password}` → 200 `{accessToken, tokenType, userId, username, role}`, 401 si las credenciales son inválidas o la cuenta está inactiva). Es la única ruta pública además de `GET /api/weather` y Swagger.
- Incidencias en `/api/issues`: `GET /api/issues` (lista, filtros opcionales `status`/`priority`), `GET /api/issues/{id}` — autenticado; `POST /api/issues` (201) y `PUT /api/issues/{id}` — `ADMIN` o `SUPPORT`; `DELETE /api/issues/{id}` (204) — `ADMIN`. CRUD completo.
- Usuarios en `/api/users` (todo el módulo exige rol `ADMIN`): `GET /api/users` (filtro opcional `active`), `GET /api/users/{id}`, `POST /api/users` (201, password con BCrypt), `PUT /api/users/{id}`, `PATCH /api/users/{id}/status` (`{isActive}`). Las respuestas nunca exponen `passwordHash`. No hay auto-registro.
- Clima en `/api/weather`: `GET /api/weather`, clima actual de Asunción (proxy de Open-Meteo; 503 si el proveedor externo falla). No usa base de datos — ver [`docs/RESTCLIENT-PROXY.md`](docs/RESTCLIENT-PROXY.md).
- Swagger: `http://localhost:8080/swagger-ui.html` — es la fuente de verdad del contrato.
- Avance del proyecto y tareas pendientes: [`docs/CHECKLIST.md`](docs/CHECKLIST.md).
- Migraciones de esquema SOLO vía changesets de Liquibase (nunca DDL manual ni `ddl-auto`).
- Variables de entorno del backend: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (obligatoria, mínimo 32 bytes: el backend no arranca sin ella).

## Convenciones de código

- **Backend**: paquetes por módulo `com.minijira.<modulo>` con subpaquetes `controller/`, `service/`, `repository/`, `entity/`, `dto/`. Sufijos: `Controller`, `Service`, `Repository`, `Dto`. Módulos actuales: `auth`, `issue`, `user`, `weather` y el transversal `common`.
- **Frontend**: componentes standalone en `src/app/features/<feature>/` con su service y sus rutas. Componentes nombrados por función, no por página.
- Booleanos con prefijo `is/has/should/can`. Archivos en `kebab-case`, clases en `PascalCase`.
- Commits: `type: descripción` (`feat`, `fix`, `docs`, `refactor`, `test`, `chore`).

## Reglas (obligatorias)

1. **No romper la arquitectura modular**: cada módulo es autocontenido; no crear dependencias cruzadas entre módulos ni saltarse capas (controller → service → repository).
2. **No agregar dependencias sin justificarlo** en el PR.
3. **No loguear secretos**: nunca contraseñas, tokens ni credenciales en logs ni en código.
4. **Todo cambio con pruebas**: como mínimo, tests en la capa donde vive la lógica y en el endpoint.
5. **PRs siempre hacia `develop`**, nunca hacia `main`. Ramas `feature/<nombre>` o `fix/<nombre>`.
6. **Un módulo no accede al `repository` de otro módulo**; se comunica a través de su `Service`.

## Recursos

- Skills de trabajo para Codex en `.agents/skills/` y para Claude en `.claude/skills/`: `backend-feature`, `frontend-feature`, `pr-ready`.
- Documentación en `docs/`.
- Reglas de colaboración en `CONTRIBUTING.md`.
