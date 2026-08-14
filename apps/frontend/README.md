# Mini Jira — Frontend

SPA en Angular 19 (componentes standalone) para el tracker de incidencias. Consume la API REST del backend en `/api/issues`.

## Desarrollo local

Requiere Node 20+.

```bash
npm install
npm start
```

La app queda en `http://localhost:4200`. El dev server usa `proxy.conf.json` para reenviar todas las llamadas a `/api` hacia `http://localhost:8080`, así que el backend tiene que estar corriendo en ese puerto.

## Build

```bash
npm run build
```

El resultado queda en `dist/frontend/browser/`.

## Docker y el proxy de nginx

El `Dockerfile` es multi-stage:

1. **build** (`node:20-alpine`): `npm ci` + `ng build`.
2. **runtime** (`nginx:alpine`): sirve el build estático en el puerto 80.

`nginx.conf` hace dos cosas:

- Sirve la SPA con fallback a `index.html` (`try_files`), para que las rutas del router de Angular funcionen al recargar.
- Proxya `/api/` hacia `http://backend:8080`, donde `backend` es el nombre del servicio en docker-compose. Por eso el código Angular usa siempre rutas relativas (`/api/...`) y nunca hardcodea el host del backend: en dev resuelve el proxy del CLI y en Docker resuelve nginx.

## Estructura

```
src/app/features/issues/
├── issue-list/        # tabla de incidencias
├── issue-form/        # alta (reactive forms)
├── issue.model.ts     # tipos del recurso Issue
├── issue.service.ts   # llamadas HTTP a /api/issues
└── issues.routes.ts   # rutas lazy de la feature
```

Rutas: `/issues` (listado) y `/issues/new` (alta). **Editar y eliminar incidencias no existen todavía**: son tareas pendientes del equipo (ver `docs/CHECKLIST.md` en la raíz).
