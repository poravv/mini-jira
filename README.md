# Mini-Jira

Plataforma interna de gestión de incidencias: registrar problemas técnicos, asignarlos, seguirlos y documentar su resolución. Una versión reducida de Jira o Linear.

El objetivo principal es **formativo**: que desarrolladores junior practiquen un flujo de trabajo profesional completo — análisis, ramas, Pull Requests, code review, pruebas, logs, Docker y uso responsable de IA. La definición completa del proyecto está en [`docs/definicion-proyecto-colaborativo-dev-jr.md`](docs/definicion-proyecto-colaborativo-dev-jr.md).

## Stack

| Área | Tecnología |
| --- | --- |
| Frontend | Angular (nginx en Docker) |
| Backend | Java 21 · Spring Boot 3 · Maven |
| BD relacional | PostgreSQL 16 (migraciones con Liquibase) |
| BD no relacional | MongoDB 7 (reservada para auditoría, aún sin uso) |
| API docs | Swagger / OpenAPI |
| Entorno local | Docker Compose |

## Estructura del monorepo

```
mini-jira/
├── apps/
│   ├── backend/          # API Spring Boot (puerto 8080)
│   └── frontend/         # SPA Angular (puerto 4200)
├── docs/                 # Definición del proyecto y arquitectura
├── .claude/skills/       # Skills para agentes de IA
├── docker-compose.yml    # Postgres + Mongo + backend + frontend
├── .env.example          # Variables de entorno de ejemplo
├── AGENTS.md             # Contexto para agentes de IA (Claude Code / Codex)
└── CONTRIBUTING.md       # Reglas de colaboración
```

## Requisitos

- **Docker Desktop** (única dependencia obligatoria).
- Opcional, para correr sin Docker: Java 21, Maven, Node 20 y Angular CLI.

## Levantar todo

```bash
cp .env.example .env
docker compose up --build
```

| Servicio | URL |
| --- | --- |
| Frontend | http://localhost:4200 |
| API | http://localhost:8080/api/issues |
| Swagger | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |
| MongoDB | localhost:27017 |

## Probar el CRUD con curl

```bash
# Crear una incidencia
curl -X POST http://localhost:8080/api/issues \
  -H "Content-Type: application/json" \
  -d '{"title": "Error al guardar", "description": "Falla el formulario de alta", "priority": "ALTA", "status": "PENDIENTE"}'

# Listar incidencias (ordenadas de la mas urgente a la menos urgente)
curl http://localhost:8080/api/issues

# Listar filtrando por estado y/o prioridad (ambos parametros son opcionales)
curl "http://localhost:8080/api/issues?status=PENDIENTE&priority=ALTA"

# Consultar una incidencia
curl http://localhost:8080/api/issues/1
```

El contrato exacto de los campos está en Swagger. Las incidencias se pueden editar con `PUT /api/issues/{id}` y eliminar con `DELETE /api/issues/{id}`.

## Modo desarrollo sin Docker

Necesitás Postgres corriendo (podés levantar solo la BD con `docker compose up postgres`).

**Backend** (puerto 8080):

```bash
cd apps/backend
./mvnw spring-boot:run
```

Usa por defecto `DB_HOST=localhost`, `DB_NAME=minijira`, `DB_USER=minijira`, `DB_PASSWORD=minijira`. Liquibase aplica las migraciones al arrancar.

**Frontend** (puerto 4200, con proxy de `/api` al backend):

```bash
cd apps/frontend
npm install
npm start   # ng serve con proxy a http://localhost:8080
```

## Flujo de trabajo Git

Basado en las secciones 9 y 10 del [documento de definición](docs/definicion-proyecto-colaborativo-dev-jr.md).

- `main`: versión estable. `develop`: integración del trabajo aprobado.
- Nadie trabaja directamente sobre `main` ni `develop`.
- Cada tarea: crear rama `feature/<nombre>` (o `fix/<nombre>`) desde `develop` → desarrollar → probar → PR hacia `develop` → code review de un compañero → correcciones → validación del líder técnico → merge.

Checklist mínimo antes de abrir el PR:

- [ ] La aplicación compila y corre localmente.
- [ ] Sin secretos ni credenciales en el código.
- [ ] Validaciones y manejo de errores incluidos.
- [ ] Logs útiles, sin datos sensibles.
- [ ] Pruebas agregadas o actualizadas.
- [ ] Documentación actualizada.
- [ ] Podés explicar tu código.

Paso a paso con diagramas (crear rama → commit → push → PR → merge a `develop`): [`docs/GIT-FLOW.md`](docs/GIT-FLOW.md). Reglas completas en [CONTRIBUTING.md](CONTRIBUTING.md).

## Backlog para juniors

El estado vivo del proyecto se sigue en [`docs/CHECKLIST.md`](docs/CHECKLIST.md) (se actualiza en cada PR). Las primeras tareas sugeridas son completar el CRUD de incidencias:

2. **Eliminar incidencia**: implementar `DELETE /api/issues/{id}` + botón en el listado (rama sugerida: `feature/eliminar-incidencia`).

Cada módulo pendiente es una funcionalidad vertical (pantalla + API + BD + pruebas). Detalle en la sección 6 del [documento de definición](docs/definicion-proyecto-colaborativo-dev-jr.md).

| Módulo | Descripción | Doc |
| --- | --- | --- |
| Autenticación JWT | Login seguro, roles, control de acceso | §6.1 |
| Usuarios | Registro, perfil, activación/desactivación | §6.1 |
| Proyectos | CRUD de proyectos y sus miembros | §6.2 |
| Comentarios | Comentarios en incidencias, permisos de autor | §6.4 |
| Auditoría (Mongo) | Historial de cambios de las incidencias | §6.5 |
| Dashboard | Métricas por estado, prioridad y actividad | §6.7 |

## Troubleshooting

- **Puerto ocupado** (`port is already allocated`): otro proceso usa 4200, 8080, 5432 o 27017. Cerralo o cambiá el mapeo en `docker-compose.yml`.
- **Contenedor caído**: `docker compose ps` para ver el estado y `docker compose logs backend` (o el servicio que falle) para leer el error.
- **Resetear la base de datos**: `docker compose down -v` borra los volúmenes y arranca desde cero en el próximo `up`.

## Documentación

| Documento | Qué es |
| --- | --- |
| [`docs/definicion-proyecto-colaborativo-dev-jr.md`](docs/definicion-proyecto-colaborativo-dev-jr.md) | Definición completa del proyecto (alcance, módulos, flujo de trabajo) |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Arquitectura: monolito modular, estructura de módulos, construido vs. pendiente |
| [`docs/GIT-FLOW.md`](docs/GIT-FLOW.md) | Flujo Git paso a paso con diagramas: rama, commit, push, Pull Request y merge a `develop` |
| [`docs/CHECKLIST.md`](docs/CHECKLIST.md) | Checklist vivo de avance del MVP (se actualiza en cada PR) |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Reglas de colaboración: ramas, commits, PRs y code review |
| [`AGENTS.md`](AGENTS.md) | Guía para asistentes IA (Claude Code / Codex): contexto y convenciones |
| [`apps/backend/README.md`](apps/backend/README.md) | Backend: cómo correrlo, endpoints, variables de entorno, Liquibase |
| [`apps/frontend/README.md`](apps/frontend/README.md) | Frontend: cómo correrlo, estructura, proxy de nginx |
| [`.github/pull_request_template.md`](.github/pull_request_template.md) | Plantilla que completa cada PR |

## Uso de IA

Este repo está preparado para trabajar con Claude Code y Codex. El contexto, las convenciones y las reglas de uso están en [AGENTS.md](AGENTS.md). Regla básica: la IA es apoyo, no reemplazo — tenés que poder explicar todo el código que entregás (sección 14 del documento de definición).
