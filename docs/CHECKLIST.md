# Checklist de avance

Estado vivo del proyecto respecto al [alcance del MVP (§7)](definicion-proyecto-colaborativo-dev-jr.md) y la infraestructura.

## Estado actual (hecho)

- [x] Monorepo con `apps/backend` y `apps/frontend` — ver `README.md`
- [x] Docker Compose levanta Postgres, Mongo, backend y frontend — `docker-compose.yml`
- [x] Migraciones de esquema con Liquibase — `apps/backend/src/main/resources/db/changelog/`
- [x] Swagger/OpenAPI publicado — `http://localhost:8080/swagger-ui.html`
- [x] Incidencias CRUD completo, backend + frontend: crear (`POST /api/issues`), listar con filtros de `status`/`priority` (`GET /api/issues`), consultar (`GET /api/issues/{id}`), editar (`PUT /api/issues/{id}` + formulario Angular), eliminar (`DELETE /api/issues/{id}` + botón con confirmación en el listado) — `apps/backend/src/main/java/com/minijira/issue/controller/IssueController.java`, `apps/frontend/src/app/features/issues/issue-list/issue-list.component.ts`
- [x] Manejo global de errores (404, 400 con detalle por campo, 503) — `apps/backend/src/main/java/com/minijira/common/GlobalExceptionHandler.java`
- [x] Documentación inicial (README, CONTRIBUTING, ARCHITECTURE, plantilla de PR, skills de IA)
- [x] Módulo weather: proxy de Open-Meteo con `RestClient` (backend) + tarjeta de clima en el header (frontend) — ver [`docs/RESTCLIENT-PROXY.md`](RESTCLIENT-PROXY.md)
- [x] Tabla `usuario` creada por Liquibase (changeset `002-create-usuario-table`) — **solo esquema**, sin código Java: avance parcial de la tarea de login, ver "Gestión de usuarios" abajo

## Tareas pendientes del MVP

Orden sugerido: cada tarea depende de que la anterior esté terminada (usuarios habilita login, login habilita asignación con usuario autenticado, etc.). Referencia funcional: sección [§6 del documento máster](definicion-proyecto-colaborativo-dev-jr.md).

### 1. Gestión de usuarios

**Objetivo**: CRUD básico de usuarios sobre la tabla `usuario` que ya existe (changeset 002), sin login todavía.
**Depende de**: nada — la tabla ya está creada.
**Backend**: módulo `com.minijira.usuario`; entidad `Usuario` mapeada a la tabla `usuario`; endpoints `GET /api/usuarios` (200), `GET /api/usuarios/{id}` (200/404), `POST /api/usuarios` (201, hashea `password_hash` con BCrypt), `PUT /api/usuarios/{id}` (200/404), `PATCH /api/usuarios/{id}/activar` y `/desactivar` (200/404). Nunca exponer `password_hash` en las respuestas.
**Frontend**: feature `users`; rutas `/usuarios` y `/usuarios/new`; `user.service.ts`.
**Pruebas mínimas**: `should_create_user_when_data_is_valid`, `should_return_404_when_user_not_found`, `should_not_expose_password_hash_in_response`.
**Documentación a actualizar**: API.md/Swagger, este checklist.
**Rama sugerida**: `feature/gestion-usuarios`.
**Definición de terminado**: [ ] endpoints documentados en Swagger [ ] password nunca viaja en texto plano ni se expone en la respuesta [ ] tests en verde [ ] ítem marcado en `CHECKLIST.md`.

### 2. Inicio de sesión con JWT

**Objetivo**: login que valida credenciales contra `usuario` y devuelve un JWT; endpoints protegidos exigen el token.
**Depende de**: Gestión de usuarios.
**Backend**: módulo `com.minijira.auth`; `POST /api/auth/login` (200 con `{token}`, 401 si credenciales inválidas); filtro/`OncePerRequestFilter` que valida el JWT en cada request; sin changeset nuevo (usa `usuario`).
**Frontend**: feature `auth`; ruta `/login`; `auth.service.ts` guarda el token (ej. en memoria o `sessionStorage`) y lo agrega a las requests con un `HttpInterceptor`; `authGuard` protege rutas de incidencias.
**Pruebas mínimas**: `should_return_token_when_credentials_are_valid`, `should_return_401_when_password_is_wrong`, `should_reject_request_when_token_is_missing`.
**Documentación a actualizar**: API.md/Swagger, diagrama de secuencia de login (§12), este checklist.
**Rama sugerida**: `feature/login-jwt`.
**Definición de terminado**: [ ] login devuelve JWT válido [ ] rutas protegidas rechazan requests sin token [ ] tests en verde [ ] diagrama de secuencia agregado.

### 3. Proyectos

**Objetivo**: CRUD de proyectos y gestión de sus miembros.
**Depende de**: Gestión de usuarios (los miembros son usuarios existentes).
**Backend**: módulo `com.minijira.proyecto`; entidad `Proyecto` + tabla `proyecto_miembro` (changeset `003-create-proyecto-tables`); endpoints `GET /api/proyectos`, `GET /api/proyectos/{id}` (200/404), `POST /api/proyectos` (201), `PUT /api/proyectos/{id}` (200/404), `POST /api/proyectos/{id}/miembros` y `DELETE /api/proyectos/{id}/miembros/{usuarioId}`.
**Frontend**: feature `projects`; rutas `/proyectos`, `/proyectos/new`, `/proyectos/:id`; `project.service.ts`.
**Pruebas mínimas**: `should_create_project_when_data_is_valid`, `should_add_member_when_user_exists`, `should_return_404_when_project_not_found`.
**Documentación a actualizar**: API.md/Swagger, MER (§12), este checklist.
**Rama sugerida**: `feature/gestion-proyectos`.
**Definición de terminado**: [ ] CRUD completo probado [ ] alta/baja de miembros probada [ ] tests en verde [ ] MER actualizado.

### 4. Asignación de incidencias a responsables

**Objetivo**: permitir asignar una incidencia a un usuario del proyecto.
**Depende de**: Gestión de usuarios, Proyectos.
**Backend**: extiende `com.minijira.issue`; columna `assignee_id` (FK a `usuario`) vía changeset `004-add-assignee-to-issues`; endpoint `PATCH /api/issues/{id}/assignee` (200/404 si la incidencia o el usuario no existen).
**Frontend**: selector de responsable en `issue-form`; columna "Asignado a" en `issue-list`.
**Pruebas mínimas**: `should_assign_issue_when_user_exists`, `should_return_404_when_assignee_does_not_exist`.
**Documentación a actualizar**: API.md/Swagger, este checklist.
**Rama sugerida**: `feature/asignacion-incidencias`.
**Definición de terminado**: [ ] endpoint documentado [ ] listado muestra el responsable [ ] tests en verde.

### 5. Reglas de estado y prioridad — **PARCIAL**

**Objetivo**: los campos `status`/`priority` ya son editables por `PUT /api/issues/{id}`; falta validar las transiciones permitidas (reglas E1-E6 / P1-P5).
**Depende de**: nada nuevo (extiende el módulo `issue` existente).
**Backend**: `com.minijira.issue`; endpoints dedicados `PATCH /api/issues/{id}/status` y `PATCH /api/issues/{id}/priority` (200 si la transición es válida, 409 si no); sin changeset nuevo.
**Frontend**: reemplazar el `<select>` libre de estado/prioridad en `issue-form` por acciones que respeten las transiciones válidas.
**Pruebas mínimas**: `should_change_status_when_transition_is_valid`, `should_reject_status_change_when_transition_is_invalid`.
**Documentación a actualizar**: crear `docs/REGLAS-ESTADO-PRIORIDAD.md` con las reglas E1-E6 / P1-P5 y enlazarlo desde acá; este checklist.
**Rama sugerida**: `feature/reglas-estado-prioridad`.
**Definición de terminado**: [ ] reglas documentadas [ ] transiciones inválidas devuelven 409 [ ] tests en verde.

### 6. Comentarios

**Objetivo**: comentar una incidencia, listar en orden cronológico, editar/eliminar comentarios propios.
**Depende de**: Gestión de usuarios (autor), Inicio de sesión (identificar quién comenta).
**Backend**: módulo `com.minijira.comentario`; tabla `comentario` (FK a `issue` y `usuario`) vía changeset `005-create-comentario-table`; endpoints `GET /api/issues/{issueId}/comentarios`, `POST /api/issues/{issueId}/comentarios` (201), `PUT /api/comentarios/{id}` (200/403 si no es el autor), `DELETE /api/comentarios/{id}` (204/403).
**Frontend**: sección de comentarios dentro del detalle de incidencia; `comment.service.ts`.
**Pruebas mínimas**: `should_list_comments_in_chronological_order`, `should_reject_edit_when_user_is_not_the_author`.
**Documentación a actualizar**: API.md/Swagger, este checklist.
**Rama sugerida**: `feature/comentarios-incidencias`.
**Definición de terminado**: [ ] permisos de autor verificados [ ] orden cronológico probado [ ] tests en verde.

### 7. Historial y auditoría (MongoDB)

**Objetivo**: registrar cambios de estado, responsable y prioridad de una incidencia, con usuario y fecha.
**Depende de**: Asignación de incidencias, Reglas de estado y prioridad.
**Backend**: módulo `com.minijira.auditoria`; colección Mongo `issue_audit` (sin Liquibase: Mongo es schemaless); se escribe desde los servicios de `issue` al cambiar estado/prioridad/responsable; endpoint `GET /api/issues/{id}/historial`.
**Frontend**: línea de tiempo en el detalle de incidencia.
**Pruebas mínimas**: `should_record_audit_entry_when_status_changes`, `should_list_history_in_order`.
**Documentación a actualizar**: ARCHITECTURE.md (Mongo pasa a "en uso"), este checklist.
**Rama sugerida**: `feature/auditoria-mongo`.
**Definición de terminado**: [ ] cada cambio relevante queda registrado [ ] historial consultable por incidencia [ ] tests en verde.

### 8. Logs estructurados y errores en Mongo

**Objetivo**: logs en JSON con id de correlación por request; persistir errores relevantes en MongoDB sin loguear secretos.
**Depende de**: Historial y auditoría (reutiliza la conexión Mongo del módulo `auditoria`).
**Backend**: filtro de correlación en `com.minijira.common`; encoder JSON en `logback-spring.xml`; colección Mongo `error_log`; `GET /api/errores` de solo lectura para consulta básica (200).
**Frontend**: no aplica (o vista mínima de errores para admin, fuera de MVP si no alcanza el tiempo).
**Pruebas mínimas**: `should_write_error_to_mongo_when_unhandled_exception_occurs`, `should_never_log_password_or_token`.
**Documentación a actualizar**: ARCHITECTURE.md, este checklist.
**Rama sugerida**: `feature/logs-estructurados`.
**Definición de terminado**: [ ] logs en JSON con id de correlación [ ] error crítico queda en Mongo [ ] sin secretos en logs (revisado a mano) [ ] tests en verde.

### 9. Dashboard básico

**Objetivo**: panel con métricas de incidencias por estado/prioridad, asignadas al usuario autenticado y actividad reciente.
**Depende de**: todo lo anterior (usuarios, asignación, estados, auditoría).
**Backend**: módulo `com.minijira.dashboard`; endpoint de solo lectura `GET /api/dashboard` (200) que agrega conteos desde `issue` y últimas entradas de `issue_audit`/`error_log`.
**Frontend**: feature `dashboard`; ruta `/dashboard` (pantalla inicial tras el login); `dashboard.service.ts`.
**Pruebas mínimas**: `should_return_issue_counts_by_status`, `should_return_recent_activity`.
**Documentación a actualizar**: API.md/Swagger, este checklist.
**Rama sugerida**: `feature/dashboard-metricas`.
**Definición de terminado**: [ ] conteos correctos verificados con datos de prueba [ ] pantalla inicial tras login [ ] tests en verde.

## Documentación pendiente exigida por el documento máster (§12)

- [ ] `docs/API.md` — resumen de endpoints, o declarar explícitamente que Swagger (`/swagger-ui.html`) lo reemplaza y enlazarlo desde el README.
- [ ] `docs/TROUBLESHOOTING.md` — problemas frecuentes y soluciones (hoy solo hay una sección corta en el README raíz).
- [ ] Colección de Postman o Bruno con los endpoints existentes.
- [ ] Diagrama de casos de uso.
- [ ] Diagrama de secuencia del inicio de sesión (se agrega junto con la tarea "Inicio de sesión con JWT").
- [ ] Diagrama de secuencia de creación de una incidencia (ejemplo ya dado en el documento máster, §12).
- [ ] Modelo o diagrama de entidades y relaciones (MER), a mantener actualizado con cada changeset de Liquibase.

## Mejoras futuras / fuera del MVP

- [x] **Módulo weather**: clima actual de Asunción vía proxy a Open-Meteo. No forma parte del alcance del MVP (§7), se registra como función adicional aprobada (§7: "las funciones adicionales deberán registrarse como mejoras futuras... con aprobación del líder técnico"). Ver [`docs/RESTCLIENT-PROXY.md`](RESTCLIENT-PROXY.md).
- [ ] **CI con GitHub Actions**: workflow que corra `mvn test` y `npm test` en cada Pull Request hacia `develop`. Hoy no existe `.github/workflows/`.

---

Este checklist es la fuente de verdad del avance; al mergear un PR, marcá el ítem.
