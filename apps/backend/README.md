# mini-jira backend

REST API del issue tracker **mini-jira** (proyecto de práctica). Spring Boot 3.4 + Java 21 + PostgreSQL + Liquibase.

Monolito modular: paquete base `com.minijira`, módulo `issue` (controller / service / repository / entity / dto / mapper / exception), módulo `user` (misma estructura; usuarios y roles `ADMIN`/`SUPPORT`/`USER`, tabla `usuario`), módulo `weather` (proxy de Open-Meteo, sin base de datos) y paquete `common` (manejo global de errores).

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
| `JWT_SECRET` | — | Secreto obligatorio de al menos 32 bytes para firmar JWT |

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/issues` | Lista todas para ADMIN/SUPPORT, o solo las asignadas para USER |
| `GET` | `/api/issues/{id}` | Obtiene una incidencia; USER solo si está asignada |
| `POST` | `/api/issues` | Crea y asigna una incidencia (ADMIN/SUPPORT) |
| `PUT` | `/api/issues/{id}` | Edita y reasigna una incidencia (ADMIN/SUPPORT) |
| `DELETE` | `/api/issues/{id}` | Elimina una incidencia (ADMIN) |
| `GET` | `/api/users` | Listar usuarios; filtro opcional `active` |
| `GET` | `/api/users/{id}` | Obtener un usuario (404 si no existe) |
| `POST` | `/api/users` | Crear usuario (201; password hasheado con BCrypt; 400 si falla validación) |
| `PUT` | `/api/users/{id}` | Editar un usuario (200; 404 si no existe) |
| `PATCH` | `/api/users/{id}/status` | Activar/desactivar con body `{"isActive": true}` (200; 404 si no existe) |
| `POST` | `/api/auth/login` | Valida credenciales y devuelve JWT, tipo Bearer, usuario y rol (200; 401 si falla) |
| `GET` | `/api/weather` | Clima actual de Asunción vía [Open-Meteo](https://open-meteo.com) (200; 503 si el proveedor falla o tarda más de 3s) |

Modelo `Issue`: `title` (requerido, máx. 150), `description` (opcional), `status` (`PENDIENTE` | `EN_PROGRESO` | `RESUELTA` | `CERRADA`, default `PENDIENTE`), `priority` (`BAJA` | `MEDIA` | `ALTA` | `CRITICA`, default `MEDIA`), `createdAt` / `updatedAt` automáticos.

Las respuestas de `/api/users` nunca incluyen `passwordHash`. La API usa Spring Security y JWT stateless; todos los endpoints salvo login, Swagger y clima requieren un Bearer token. Usuario por defecto **solo para desarrollo**: `admin` / `admin123` (changeset `003`).

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

Changesets existentes: `001-create-issues-table` (tabla `issues`), `002-create-usuario-table` (tabla `usuario`), `003-insert-admin-user` (usuario `admin`/`admin123`, solo dev) y `004-add-issue-assignee` (asignación de incidencias).

### Agregar un changeset

1. Crear un archivo nuevo en `src/main/resources/db/changelog/`, por ejemplo `004-create-proyecto-tables.yaml`, con un `changeSet` de `id` único y `author`.
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
```

3. Arrancar la app: Liquibase aplica el changeset y lo registra en la tabla `databasechangelog`.

Nunca editar un changeset ya aplicado (Liquibase valida checksums); siempre crear uno nuevo.
## Seguridad

La API requiere un JWT en `Authorization: Bearer <token>` para todos los endpoints salvo
`POST /api/auth/login`, Swagger y `GET /api/weather`. Definí `JWT_SECRET` con al menos 32 bytes
antes de iniciar el backend. El login responde el token y el rol actual del usuario.

Roles:

- `ADMIN`: administra usuarios y puede ver, crear, actualizar y eliminar cualquier incidencia.
- `SUPPORT`: puede ver, crear y asignar incidencias a usuarios activos.
- `USER`: solo puede listar y consultar sus incidencias asignadas.
