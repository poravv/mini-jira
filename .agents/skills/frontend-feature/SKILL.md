---
name: frontend-feature
description: Cómo agregar una feature Angular siguiendo la estructura del proyecto, con service y rutas. Usar al crear o modificar funcionalidad en apps/frontend.
---

# Agregar una feature al frontend Angular

## Estructura obligatoria

Cada feature vive en `src/app/features/<feature>/` con componentes standalone:

```
src/app/features/<feature>/
├── <feature>-list/       # componente de listado
├── <feature>-form/       # componente de alta/edición (si aplica)
├── <feature>.service.ts  # llamadas HTTP al backend
├── <feature>.model.ts    # interfaces TypeScript del recurso
└── <feature>.routes.ts   # rutas de la feature (lazy)
```

## Pasos

1. Mirá la feature `issues` existente como referencia de estilo.
2. Definí las interfaces en `<feature>.model.ts` espejando los DTO del backend (verificá el contrato en Swagger).
3. Creá el service con `HttpClient` apuntando a `/api/<recurso>` (ruta relativa: el proxy de dev y nginx la resuelven — nunca hardcodees `http://localhost:8080`).
4. Creá los componentes standalone; la lógica de datos va en el service, el componente solo presenta.
5. Registrá las rutas de la feature y colgalas del router principal con lazy loading.
6. Manejá estados de carga y error en la UI (no dejes fallos silenciosos).

## Convenciones

- Archivos en `kebab-case`, clases en `PascalCase`, booleanos con `is/has/should`.
- Componentes nombrados por lo que SON (`issue-list`), no por la página.
- Formularios con Reactive Forms y validaciones que espejen las del backend.

## Antes de terminar

- `npm test` en verde y la app compila (`ng build`).
- Probala contra el backend real (`docker compose up` o `ng serve` + backend local).
- Seguí la skill `pr-ready` antes de abrir el PR.
