# mini-jira backend

REST API del issue tracker **mini-jira** (proyecto de práctica). Spring Boot 3.4 + Java 21 + PostgreSQL + Liquibase.

Monolito modular: paquete base `com.minijira`, módulos `issue`, `user`, `auth` y `proyecto`, módulo `weather` (proxy de Open-Meteo, sin base de datos) y paquete `common` (manejo global de errores).

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
| `JWT_SECRET` | clave local de desarrollo | Clave Base64 (mínimo 32 bytes) para firmar JWT |
| `JWT_ISSUER` | `mini-jira` | Emisor incluido en el token |
| `JWT_EXPIRATION_SECONDS` | `3600` | Duración del token |

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/auth/login` | Autentica por usuario/email y devuelve `{ token, tokenType, expiresIn, user }` |
| `GET` | `/api/issues` | Listar issues; acepta `status`, `priority`, `projectId` y `assigneeId` (opcionales) y devuelve las mas urgentes primero |
| `GET` | `/api/issues/{id}` | Obtener un issue (404 si no existe) |
| `POST` | `/api/issues` | Crear issue (201; 400 con errores por campo si falla validación) |
| `PUT` | `/api/issues/{id}` | Editar un issue (200; 404 si no existe; 400 si falla validación) |
| `DELETE` | `/api/issues/{id}` | Eliminar un issue (204; 404 si no existe) |
| `GET` | `/api/proyectos` | Listar proyectos (requiere JWT) |
| `GET` | `/api/proyectos/{id}` | Obtener un proyecto con sus miembros (404 si no existe) |
| `POST` | `/api/proyectos` | Crear proyecto (201; 400 si falla validación) |
| `PUT` | `/api/proyectos/{id}` | Editar proyecto (200; 404 si no existe) |
| `DELETE` | `/api/proyectos/{id}` | Eliminar proyecto (204; 404 si no existe) |
| `POST` | `/api/proyectos/{id}/miembros` | Agregar un usuario existente (ADMIN; body `{ userId }`) |
| `DELETE` | `/api/proyectos/{id}/miembros/{userId}` | Quitar un miembro existente (ADMIN; 204) |
| `GET` | `/api/weather` | Clima actual de Asunción vía [Open-Meteo](https://open-meteo.com) (200; 503 si el proveedor falla o tarda más de 3s) |

El registro (`POST /api/users`) y el clima son públicos. El login también se mantiene disponible como `POST /api/users/login` por compatibilidad. Todas las operaciones de incidencias y proyectos requieren `Authorization: Bearer <token>`. La consulta y administración de usuarios, y la gestión de miembros de proyectos, requieren el rol `ADMIN` cuando corresponde.

Modelo `Issue`: `title` (requerido, máx. 150), `description` (opcional), `status` (`PENDIENTE` | `EN_PROGRESO` | `RESUELTA` | `CERRADA`, default `PENDIENTE`), `priority` (`BAJA` | `MEDIA` | `ALTA` | `CRITICA`, default `MEDIA`), `projectId` (opcional), `assigneeId` (opcional), `createdAt` / `updatedAt` automáticos. Si se informa `assigneeId`, también debe informarse `projectId` y el usuario debe ser miembro de ese proyecto. La respuesta incluye `projectId`, `projectName` y el objeto `assignee`.

No hay configuración de CORS: el frontend siempre llama a `/api` con rutas relativas a través de un proxy (el dev server de Angular en desarrollo, nginx en Docker), así que el navegador nunca hace una petición cross-origin.

## Módulo weather

`GET /api/weather` es un proxy hacia Open-Meteo: el backend consulta el proveedor externo y devuelve un contrato propio, `{ "city": "Asunción", "temperature": 24.1, "humidity": 60, "weatherCode": 3, "windSpeed": 12.3 }` (503 si Open-Meteo no responde o responde incompleto).

Configuración en `application.yml`:

- `weather.open-meteo.url`: URL base del proveedor (`https://api.open-meteo.com/v1/forecast`).
- `spring.http.client.connect-timeout` / `read-timeout`: 3s cada uno — un proveedor lento no debe colgar la API.

Explicación completa capa por capa, cómo probarlo y cómo replicar el patrón para otro servicio externo: [`docs/RESTCLIENT-PROXY.md`](../../docs/RESTCLIENT-PROXY.md).

## Migraciones con Liquibase

El esquema lo maneja Liquibase (Hibernate solo valida: `ddl-auto: validate`). Al arrancar la app, Liquibase aplica los changesets pendientes registrados en:

```
src/main/resources/db/changelog/db.changelog-master.yaml
```

Changesets existentes: `001-create-issues-table`, `002-create-usuario-table`, `003-insert-admin-user`, `004-create-proyecto-tables` y `005-add-issue-project-assignee`.

### Agregar un changeset

1. Crear un archivo nuevo en `src/main/resources/db/changelog/`, por ejemplo `006-create-comentario-table.yaml`, con un `changeSet` de `id` único y `author`.
2. Incluirlo al final del master:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/001-create-issues-table.yaml
  - include:
      file: db/changelog/002-create-usuario-table.yaml
  - include:
      file: db/changelog/003-insert-admin-user.yaml
  - include:
      file: db/changelog/004-create-proyecto-tables.yaml
  - include:
      file: db/changelog/005-add-issue-project-assignee.yaml
```

3. Arrancar la app: Liquibase aplica el changeset y lo registra en la tabla `databasechangelog`.

Nunca editar un changeset ya aplicado (Liquibase valida checksums); siempre crear uno nuevo.
