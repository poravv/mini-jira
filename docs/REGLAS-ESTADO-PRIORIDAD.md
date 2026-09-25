# Reglas de estado y prioridad

## Objetivo y fuente de verdad

Este documento describe el comportamiento vigente de las incidencias. Se verificó contra `IssueTransitionPolicy`, `IssueService`, `IssueController`, los DTO de actualización, los tests de backend y el formulario Angular.

Los únicos valores implementados son:

| Campo | Valores |
| --- | --- |
| `status` | `PENDIENTE`, `EN_PROGRESO`, `RESUELTA`, `CERRADA` |
| `priority` | `BAJA`, `MEDIA`, `ALTA`, `CRITICA` |

Los nombres y significados que siguen se limitan a las etiquetas de los enums y a sus destinos permitidos. El código no define una semántica de negocio adicional.

## Estados

| Estado | Nombre | Significado implementado | Destinos permitidos por API | Destinos no permitidos por API |
| --- | --- | --- | --- | --- |
| `PENDIENTE` | `PENDIENTE` | El enum no aporta otra definición; permite los destinos de esta fila. | `PENDIENTE`, `EN_PROGRESO`, `CERRADA` | `RESUELTA` |
| `EN_PROGRESO` | `EN_PROGRESO` | El enum no aporta otra definición; permite los destinos de esta fila. | `EN_PROGRESO`, `PENDIENTE`, `RESUELTA` | `CERRADA` |
| `RESUELTA` | `RESUELTA` | El enum no aporta otra definición; permite los destinos de esta fila. | `RESUELTA`, `EN_PROGRESO`, `CERRADA` | `PENDIENTE` |
| `CERRADA` | `CERRADA` | Estado sin destinos distintos de sí mismo en la política. | `CERRADA` | `PENDIENTE`, `EN_PROGRESO`, `RESUELTA` |

Conservar el mismo estado es válido e idempotente en la API. Las demás transiciones prohibidas producen `409 Conflict` y se validan antes de asignar el valor o guardar la incidencia.

### Reglas E1-E6

| Regla | Nombre | Significado / transición permitida | Transiciones no permitidas desde el mismo origen |
| --- | --- | --- | --- |
| E1 | `PENDIENTE` → `EN_PROGRESO` | Permite ese cambio exacto. | `PENDIENTE` → `RESUELTA` |
| E2 | `PENDIENTE` → `CERRADA` | Permite ese cambio exacto. | `PENDIENTE` → `RESUELTA` |
| E3 | `EN_PROGRESO` → `PENDIENTE` | Permite ese cambio exacto. | `EN_PROGRESO` → `CERRADA` |
| E4 | `EN_PROGRESO` → `RESUELTA` | Permite ese cambio exacto. | `EN_PROGRESO` → `CERRADA` |
| E5 | `RESUELTA` → `EN_PROGRESO` | Permite ese cambio exacto. | `RESUELTA` → `PENDIENTE` |
| E6 | `RESUELTA` → `CERRADA` | Permite ese cambio exacto. | `RESUELTA` → `PENDIENTE` |

La permanencia en el estado de origen se permite para los cuatro estados, pero no constituye una regla E1-E6. Desde `CERRADA` no existe una regla de salida.

## Prioridades

La política de backend permite cualquier combinación de prioridades válidas, incluida conservar el mismo valor. Por ello no hay transiciones de prioridad de negocio prohibidas entre valores del enum. Un valor ausente o ajeno al enum es un error de entrada (`400`), no una transición inválida (`409`).

| Prioridad | Nombre | Significado implementado |
| --- | --- | --- |
| `BAJA` | `BAJA` | Valor válido de prioridad; no tiene restricciones de destino. |
| `MEDIA` | `MEDIA` | Valor válido de prioridad; no tiene restricciones de destino. |
| `ALTA` | `ALTA` | Valor válido de prioridad; no tiene restricciones de destino. |
| `CRITICA` | `CRITICA` | Valor válido de prioridad; no tiene restricciones de destino. |

### Reglas P1-P5

| Regla | Nombre | Origen | Transiciones permitidas | Transiciones no permitidas |
| --- | --- | --- | --- | --- |
| P1 | `BAJA` → prioridad válida | `BAJA` | `BAJA`, `MEDIA`, `ALTA`, `CRITICA` | Ninguna entre valores válidos. |
| P2 | `MEDIA` → prioridad válida | `MEDIA` | `BAJA`, `MEDIA`, `ALTA`, `CRITICA` | Ninguna entre valores válidos. |
| P3 | `ALTA` → prioridad válida | `ALTA` | `BAJA`, `MEDIA`, `ALTA`, `CRITICA` | Ninguna entre valores válidos. |
| P4 | `CRITICA` → prioridad válida | `CRITICA` | `BAJA`, `MEDIA`, `ALTA`, `CRITICA` | Ninguna entre valores válidos. |
| P5 | Conservación idempotente | Cualquier prioridad | Mantener el mismo valor. También está incluido en P1-P4. | Ninguna entre valores válidos. |

P1-P5 son identificadores de estas reglas de la tarea; el backend las implementa como una única política que acepta origen y destino no nulos.

## API

Ambos endpoints requieren autenticación y rol `ADMIN` o `SUPPORT`. Reciben y devuelven JSON. Buscan primero la incidencia y modifican únicamente el campo dedicado por el endpoint.

### `PATCH /api/issues/{id}/status`

Actualiza `status` conforme a la matriz E1-E6.

```json
{
  "status": "EN_PROGRESO"
}
```

`status` debe ser uno de `PENDIENTE`, `EN_PROGRESO`, `RESUELTA` o `CERRADA`.

### `PATCH /api/issues/{id}/priority`

Actualiza `priority` conforme a P1-P5.

```json
{
  "priority": "CRITICA"
}
```

`priority` debe ser uno de `BAJA`, `MEDIA`, `ALTA` o `CRITICA`.

### Respuesta exitosa

Los dos endpoints devuelven `200 OK` y la representación completa del Issue:

```json
{
  "id": 1,
  "title": "Fix registration",
  "description": null,
  "status": "EN_PROGRESO",
  "priority": "MEDIA",
  "createdAt": "2026-08-17T10:00:00Z",
  "updatedAt": "2026-08-17T10:05:00Z"
}
```

El endpoint de estado solo asigna `status`; el de prioridad solo asigna `priority`. Los demás datos de la respuesta son la representación vigente de la incidencia.

### Errores

| Situación | HTTP | Respuesta implementada | Efecto sobre la incidencia |
| --- | --- | --- | --- |
| Id inexistente | `404 Not Found` | `{"error":"Issue not found: {id}"}` | No se modifica. |
| Campo ausente o `null` | `400 Bad Request` | `{"error":"Validation failed","fields":{"status" o "priority":"must not be null"}}` | No se modifica. |
| JSON nulo, malformado o literal de enum desconocido | `400 Bad Request` | `{"error":"Invalid request body"}` | No se modifica. |
| Transición de estado no permitida | `409 Conflict` | `{"error":"Invalid status transition: ORIGEN -> DESTINO"}` | No se modifica; no se llama a `save`. |
| Transición de prioridad inválida | `409 Conflict` si la política la rechazase | La misma forma, con `priority`. | Con la matriz P1-P5 no ocurre para dos prioridades válidas. |

## Relación con el frontend

El formulario de edición calcula sus botones desde las mismas matrices:

- Para estado, ofrece solo los destinos E1-E6 distintos del valor actual. No muestra acciones para conservar el mismo estado, aunque la API las acepte.
- Para prioridad, ofrece las otras tres prioridades válidas. No muestra conservar la prioridad actual, aunque la API lo acepte.
- Durante una llamada PATCH bloquea nuevas acciones. Si recibe `409`, muestra un mensaje específico y hace `GET /api/issues/{id}` para sincronizar estado, prioridad y las acciones disponibles, sin sobrescribir título ni descripción que aún no se hubieran guardado.

## Verificación realizada

Se contrastó esta documentación con:

- Backend: `IssueTransitionPolicy`, `IssueService`, DTOs de PATCH, `IssueController` y `GlobalExceptionHandler`.
- Tests de backend: permisos de E1-E6, cierre terminal, actualización válida, `400`, `404`, `409` y ausencia de guardado ante una transición de estado rechazada.
- Frontend: `issue-transition.rules.ts`, `IssueService` y `IssueFormComponent`, incluidos los tests de PATCH y resincronización tras `409`.

`PUT /api/issues/{id}` se conserva como parte del CRUD y no aplica la política de transición dedicada; estas reglas se aplican en los dos PATCH documentados.
