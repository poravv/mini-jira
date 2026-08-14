# Contexto para agentes de IA (Claude Code / Codex)

## Qué es este proyecto

Mini-Jira: plataforma de gestión de incidencias con fines formativos para desarrolladores junior. Monorepo con monolito modular. La definición completa está en `docs/definicion-proyecto-colaborativo-dev-jr.md` y la arquitectura en `docs/ARCHITECTURE.md`.

## Stack

- **Backend** (`apps/backend`): Java 21, Spring Boot 3, Maven, PostgreSQL 16, Liquibase, Swagger. Puerto 8080.
- **Frontend** (`apps/frontend`): Angular con componentes standalone, nginx en Docker (puerto 4200 → 80), proxy de `/api/` al backend.
- **MongoDB**: presente en compose, reservado para el futuro módulo de auditoría. El backend NO lo usa todavía.
- **Sin autenticación** en esta versión: JWT es una tarea futura de los juniors.

## Contrato de la API

- Incidencias en `/api/issues`: GET lista, GET por id y POST. **No existen PUT ni DELETE a propósito** — editar y eliminar son tareas pendientes del equipo (ver `docs/CHECKLIST.md`).
- Swagger: `http://localhost:8080/swagger-ui.html` — es la fuente de verdad del contrato.
- Migraciones de esquema SOLO vía changesets de Liquibase (nunca DDL manual ni `ddl-auto`).
- Variables de entorno del backend: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

## Convenciones de código

- **Backend**: paquetes por módulo `com.minijira.<modulo>` con subpaquetes `controller/`, `service/`, `repository/`, `entity/`, `dto/`. Sufijos: `Controller`, `Service`, `Repository`, `Dto`.
- **Frontend**: componentes standalone en `src/app/features/<feature>/` con su service y sus rutas. Componentes nombrados por función, no por página.
- Booleanos con prefijo `is/has/should/can`. Archivos en `kebab-case`, clases en `PascalCase`.
- Commits: `type: descripción` (`feat`, `fix`, `docs`, `refactor`, `test`, `chore`).

## Reglas (obligatorias)

1. **No romper la arquitectura modular**: cada módulo es autocontenido; no crear dependencias cruzadas entre módulos ni saltarse capas (controller → service → repository).
2. **No agregar dependencias sin justificarlo** en el PR.
3. **No loguear secretos**: nunca contraseñas, tokens ni credenciales en logs ni en código.
4. **Todo cambio con pruebas**: como mínimo, tests en la capa donde vive la lógica y en el endpoint.
5. **PRs siempre hacia `develop`**, nunca hacia `main`. Ramas `feature/<nombre>` o `fix/<nombre>`.

## Recursos

- Skills de trabajo en `.claude/skills/`: `backend-feature`, `frontend-feature`, `pr-ready`.
- Documentación en `docs/`.
- Reglas de colaboración en `CONTRIBUTING.md`.
