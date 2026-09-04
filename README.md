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
| Autenticación | JWT stateless (Spring Security + jjwt) |
| API docs | Swagger / OpenAPI |
| Entorno local | Docker Compose |

## Estructura del monorepo

```
mini-jira/
├── apps/
│   ├── backend/          # API Spring Boot (puerto 8080)
│   └── frontend/         # SPA Angular (puerto 4200)
├── docs/                 # Definición del proyecto y arquitectura
├── .claude/skills/       # Skills para agentes de IA (Claude)
├── .agents/skills/       # Skills para agentes de IA (Codex), copia idéntica
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
# JWT_SECRET es obligatorio (mínimo 32 caracteres); sin él el backend no arranca
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

Salvo `/api/auth/login` y `/api/weather`, todos los endpoints exigen `Authorization: Bearer <token>`.

```bash
# 1. Obtener el token (admin/admin123 es el usuario semilla de desarrollo)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier": "admin", "password": "admin123"}' | jq -r .accessToken)

# 2. Crear una incidencia (requiere rol ADMIN o SUPPORT)
curl -X POST http://localhost:8080/api/issues \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"title": "Error al guardar", "description": "Falla el formulario de alta", "priority": "ALTA", "status": "PENDIENTE"}'

# Listar incidencias (ordenadas de la mas urgente a la menos urgente)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/issues

# Listar filtrando por estado y/o prioridad (ambos parametros son opcionales)
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/issues?status=PENDIENTE&priority=ALTA"

# Consultar una incidencia
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/issues/1

# Clima actual de Asunción (proxy de Open-Meteo) — ruta pública
curl http://localhost:8080/api/weather
```

El contrato exacto de los campos está en Swagger. Las incidencias se pueden editar con `PUT /api/issues/{id}` (ADMIN o SUPPORT) y eliminar con `DELETE /api/issues/{id}` (ADMIN).

## Modo desarrollo sin Docker

Necesitás Postgres corriendo (podés levantar solo la BD con `docker compose up postgres`).

**Backend** (puerto 8080):

```bash
cd apps/backend
./mvnw spring-boot:run
```

Usa por defecto `DB_HOST=localhost`, `DB_NAME=minijira`, `DB_USER=minijira`, `DB_PASSWORD=minijira`. `JWT_SECRET` no tiene default: exportala antes de arrancar. Liquibase aplica las migraciones al arrancar.

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

El CRUD de incidencias, la gestión de usuarios (`/api/users`) y la autenticación JWT (`/api/auth/login`, con control de roles) ya están completos. El estado vivo del proyecto y el detalle de cada tarea pendiente del MVP (objetivo, endpoints, changesets, pruebas mínimas, rama sugerida) se siguen en [`docs/CHECKLIST.md`](docs/CHECKLIST.md) — es la fuente de verdad del avance y se actualiza en cada PR.

Cada módulo pendiente es una funcionalidad vertical (pantalla + API + BD + pruebas). Detalle funcional en la sección 6 del [documento de definición](docs/definicion-proyecto-colaborativo-dev-jr.md); orden de ejecución sugerido y tareas concretas en `docs/CHECKLIST.md`.

| Módulo | Descripción | Doc |
| --- | --- | --- |
| Proyectos | CRUD de proyectos y sus miembros | §6.2 |
| Reglas de estado/prioridad | Transiciones válidas (hoy editable libremente por PUT) | §6.3 |
| Comentarios | Comentarios en incidencias, permisos de autor | §6.4 |
| Auditoría (Mongo) | Historial de cambios de las incidencias | §6.5 |
| Logs estructurados | Logs JSON, correlación de requests, errores en Mongo | §6.6 |
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
| [`docs/RESTCLIENT-PROXY.md`](docs/RESTCLIENT-PROXY.md) | Cómo consumir un servicio externo con `RestClient` (patrón proxy), con el módulo weather como ejemplo |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Reglas de colaboración: ramas, commits, PRs y code review |
| [`AGENTS.md`](AGENTS.md) | Guía para asistentes IA (Claude Code / Codex): contexto y convenciones |
| [`apps/backend/README.md`](apps/backend/README.md) | Backend: cómo correrlo, endpoints, variables de entorno, Liquibase |
| [`apps/frontend/README.md`](apps/frontend/README.md) | Frontend: cómo correrlo, estructura, proxy de nginx |
| [`.github/pull_request_template.md`](.github/pull_request_template.md) | Plantilla que completa cada PR |

## Uso de IA

Este repo está preparado para trabajar con Claude Code y Codex. El contexto, las convenciones y las reglas de uso están en [AGENTS.md](AGENTS.md). Regla básica: la IA es apoyo, no reemplazo — tenés que poder explicar todo el código que entregás (sección 14 del documento de definición).
