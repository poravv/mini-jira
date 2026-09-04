# Mini Jira — Frontend

SPA en Angular (componentes standalone) para el tracker de incidencias. El login obtiene un JWT desde `/api/auth/login`; el interceptor lo envía como `Authorization: Bearer ...` y el guard protege las rutas de incidencias.

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
├── issue-list/        # tabla de incidencias, con botón eliminar
├── issue-form/        # alta y edición (reactive forms)
├── issue.model.ts     # tipos del recurso Issue
├── issue.service.ts   # llamadas HTTP a /api/issues (GET, GET id, POST, PUT, DELETE)
└── issues.routes.ts   # rutas lazy de la feature
```

Rutas: `/issues` (listado), `/issues/new` (alta) y `/issues/:id/edit` (edición, mismo `IssueFormComponent` que el alta). En el listado, cada fila tiene un botón para eliminar la incidencia: pide confirmación con `window.confirm` y, si se confirma, llama a `DELETE /api/issues/{id}` y la saca de la tabla.

## Módulo weather

`WeatherCardComponent` (`src/app/features/weather/weather-card/`) se muestra en el header de `AppComponent`, siempre visible en cualquier ruta. Pide el clima actual con `WeatherService.getCurrent()` (`GET /api/weather`) y maneja tres estados: cargando, error (si el backend devuelve 503) y clima mostrado. El código WMO del clima se traduce a una descripción corta en español con `describeWeatherCode()` (`weather.model.ts`). Más detalle del proxy contra Open-Meteo en el backend: [`docs/RESTCLIENT-PROXY.md`](../../docs/RESTCLIENT-PROXY.md).
