# mini-jira backend

REST API del issue tracker **mini-jira** (proyecto de práctica). Spring Boot 3.4 + Java 21 + PostgreSQL + Liquibase.

Monolito modular: paquete base `com.minijira`, módulo `issue` (controller / service / repository / entity / dto / mapper / exception) y paquete `common` (manejo global de errores).

## Requisitos

- Java 21
- Maven 3.9+ (solo para correr sin Docker)
- PostgreSQL 15+ accesible (local o vía docker compose del monorepo)

## Correr localmente (sin Docker)

Con un Postgres local que tenga la base `minijira` y el usuario `minijira`/`minijira`:

```bash
mvn spring-boot:run
```

La API queda en `http://localhost:8080` y Swagger UI en `http://localhost:8080/swagger-ui.html`.

Tests (no requieren base de datos):

```bash
mvn test
```

## Correr con Docker

```bash
docker build -t mini-jira-backend .
docker run -p 8080:8080 -e DB_HOST=host.docker.internal mini-jira-backend
```

## Variables de entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `DB_NAME` | `minijira` | Nombre de la base |
| `DB_USER` | `minijira` | Usuario de la base |
| `DB_PASSWORD` | `minijira` | Password de la base |

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/issues` | Listar issues |
| `GET` | `/api/issues/{id}` | Obtener un issue (404 si no existe) |
| `POST` | `/api/issues` | Crear issue (201; 400 con errores por campo si falla validación) |

**A propósito no existen `PUT` ni `DELETE`**: editar y eliminar incidencias son tareas pendientes del equipo (ver backlog en el README raíz y `docs/CHECKLIST.md`).

Modelo `Issue`: `title` (requerido, máx. 150), `description` (opcional), `status` (`PENDIENTE` | `EN_PROGRESO` | `RESUELTA` | `CERRADA`, default `PENDIENTE`), `priority` (`BAJA` | `MEDIA` | `ALTA` | `CRITICA`, default `MEDIA`), `createdAt` / `updatedAt` automáticos.

No hay configuración de CORS: el frontend siempre llama a `/api` con rutas relativas a través de un proxy (el dev server de Angular en desarrollo, nginx en Docker), así que el navegador nunca hace una petición cross-origin.

## Migraciones con Liquibase

El esquema lo maneja Liquibase (Hibernate solo valida: `ddl-auto: validate`). Al arrancar la app, Liquibase aplica los changesets pendientes registrados en:

```
src/main/resources/db/changelog/db.changelog-master.yaml
```

### Agregar un changeset

1. Crear un archivo nuevo en `src/main/resources/db/changelog/`, por ejemplo `002-add-assignee-to-issues.yaml`, con un `changeSet` de `id` único y `author`.
2. Incluirlo al final del master:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/001-create-issues-table.yaml
  - include:
      file: db/changelog/002-add-assignee-to-issues.yaml
```

3. Arrancar la app: Liquibase aplica el changeset y lo registra en la tabla `databasechangelog`.

Nunca editar un changeset ya aplicado (Liquibase valida checksums); siempre crear uno nuevo.
