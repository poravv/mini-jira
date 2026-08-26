# Checklist de avance

Estado vivo del proyecto respecto al [alcance del MVP (§7)](definicion-proyecto-colaborativo-dev-jr.md) y la infraestructura. **Se actualiza en cada PR**: al completar un objetivo, marcá su casilla en el mismo PR.

## Base del proyecto (estado actual)

- [x] Monorepo con `apps/backend` y `apps/frontend`
- [x] Docker Compose levanta Postgres, Mongo, backend y frontend
- [x] Migraciones de esquema con Liquibase
- [x] Swagger/OpenAPI publicado
- [x] Incidencias: crear, listar, consultar por id, editar y eliminar
- [x] Manejo global de errores y validaciones de ejemplo (400 por campo, 404)
- [x] Documentación inicial (README, CONTRIBUTING, ARCHITECTURE, plantilla de PR, skills IA)

## MVP pendiente (tareas para el equipo)

- [x] Editar incidencia (`PUT /api/issues/{id}` + formulario)
- [x] Eliminar incidencia: API `DELETE /api/issues/{id}` terminada; botón en el listado pendiente (arturito)
- [x] Inicio de sesión con JWT(arturito)
- [x] Gestión básica de usuarios (`/api/users` + listado, alta, edición y activación/desactivación)(david)
- [ ] Creación y consulta de proyectos
- [ ] Asignación de incidencias a responsables
- [ ] Cambios de estado y prioridad con reglas de negocio
- [ ] Comentarios en incidencias
- [ ] Historial de modificaciones (auditoría en MongoDB)
- [ ] Logs estructurados y registro de errores en MongoDB
- [ ] Dashboard básico de métricas
