# Informe de cambios incorporados

**Alcance:** comparación entre la base inicial `main` y el estado actual de `develop`.

## Resumen ejecutivo

El proyecto pasó de un CRUD inicial de incidencias a una aplicación con autenticación JWT, control de acceso por roles, administración de usuarios, filtros y edición de incidencias, clima de Asunción y una interfaz Angular que integra esas funciones. También se incorporaron pruebas, migraciones de base de datos, documentación técnica y guías de trabajo para agentes de IA.

La comparación contiene **108 archivos modificados o agregados**. El total de líneas no es un indicador directo de código nuevo porque incluye la actualización del archivo de bloqueo de dependencias de Angular y documentación.

## Funcionalidades agregadas

### Incidencias

- Se completó el CRUD de incidencias: creación, listado, consulta por identificador, edición y eliminación.
- El listado acepta filtros opcionales por estado y prioridad, tanto desde el backend como desde la interfaz.
- Las incidencias se ordenan de mayor a menor urgencia.
- La interfaz dispone de formulario de alta/edición y de eliminación con confirmación.
- Se restringieron las operaciones: cualquier usuario autenticado puede consultar; `ADMIN` y `SUPPORT` pueden crear y editar; solo `ADMIN` puede eliminar.

### Usuarios y roles

- Se añadió el módulo backend `user`, con entidad `User`, roles `ADMIN`, `SUPPORT` y `USER`, repositorio, servicio, DTOs, mapeador y manejo de errores específicos.
- Se crearon los endpoints administrativos:
  - `GET /api/users?active=` y `GET /api/users/{id}`.
  - `POST /api/users` para alta de usuarios.
  - `PUT /api/users/{id}` para actualización.
  - `PATCH /api/users/{id}/status` para activar o desactivar cuentas.
- Las respuestas de usuarios excluyen el hash de contraseña y las contraseñas se almacenan usando BCrypt.
- Se incorporaron los changesets Liquibase `002-create-usuario-table` y `003-insert-admin-user`; este último crea un administrador de desarrollo de manera idempotente.
- En Angular se incorporó la sección de usuarios con listado, formulario de alta/edición, servicio, modelos y rutas protegidas con `adminGuard`.

### Inicio de sesión y seguridad

- Se creó el módulo `auth` y el endpoint público `POST /api/auth/login`.
- El inicio de sesión acepta identificador y contraseña, y devuelve token de acceso, tipo de token, id, usuario y rol cuando las credenciales son válidas y la cuenta está activa.
- Se implementó JWT HS256, con secreto obligatorio mediante `JWT_SECRET` y vencimiento configurable, actualmente `PT8H`.
- La seguridad del backend es stateless: un filtro valida el encabezado `Authorization: Bearer <token>` en cada petición protegida.
- Swagger y `GET /api/weather` permanecen públicos. Las demás rutas requieren autenticación, complementada con autorización por rol mediante `@PreAuthorize`.
- Se eliminó la alternativa de inicio de sesión bajo el módulo de usuarios para centralizar esa responsabilidad en `auth`.
- El frontend suma página de login, persistencia de sesión, `authGuard`, `adminGuard` e interceptor HTTP. El interceptor adjunta el token a `/api/**`, excepto al login, y cierra la sesión/redirige ante una respuesta 401.

### Clima de Asunción

- Se añadió `GET /api/weather`, un proxy sin persistencia hacia Open-Meteo para consultar el clima actual de Asunción.
- El backend transforma la respuesta externa a un DTO propio y responde 503 si el proveedor no está disponible.
- La interfaz incluye una tarjeta de clima en el encabezado, junto con servicio, modelo y pruebas.

## Cambios de interfaz y experiencia

- Se ampliaron las rutas de Angular para cargar de forma diferida las funcionalidades de autenticación, incidencias y usuarios.
- Se renovaron el encabezado, estilos globales, vista de incidencias y formularios para incorporar navegación, filtros, acciones de edición/eliminación y estado de sesión.
- Se añadieron los recursos visuales `sky-background.png` y `user-logo.png`.
- La configuración de Angular y nginx se ajustó para el funcionamiento de la aplicación y el proxy de API.

## Calidad, configuración y documentación

- Se añadieron pruebas unitarias/de controlador para `auth`, `user`, `weather` e incidencias; el frontend incorpora pruebas para los servicios de autenticación, incidencias, usuarios, clima e interceptor.
- Se actualizó el manejo global de errores para contemplar errores de autenticación, usuarios y proveedor de clima.
- Se incorporaron las dependencias y configuración necesarias para Spring Security, JWT, BCrypt y el cliente HTTP del backend.
- Se actualizó `.env.example`, la configuración de aplicación, Docker Compose y los README de raíz, backend y frontend para documentar las variables y el uso de JWT.
- Se añadieron `docs/GIT-FLOW.md`, `docs/RESTCLIENT-PROXY.md` y la actualización de `docs/ARCHITECTURE.md` con diagramas del login y de una solicitud protegida.
- Se actualizó `docs/CHECKLIST.md` y se agregaron skills de trabajo para backend, frontend y preparación de pull requests.

## Estado del alcance

Ya están implementados los módulos de incidencias, usuarios, autenticación y la mejora adicional de clima. Siguen pendientes para el MVP: proyectos, asignación de responsables, reglas completas de transición de estados, comentarios, auditoría en MongoDB, logs estructurados, dashboard y parte de la documentación operativa (colección de API, MER y diagramas restantes).

## Consideraciones para despliegue

- `JWT_SECRET` es obligatorio y debe tener al menos 32 bytes; no debe reutilizarse el valor de desarrollo en entornos reales.
- El usuario administrador insertado por Liquibase está marcado explícitamente como dato de desarrollo y debe sustituirse o gestionarse de forma segura antes de producción.
- La disponibilidad de `/api/weather` depende de Open-Meteo; la interfaz debe manejar la respuesta 503 prevista.

## Validación de este informe

El informe se elaboró a partir del historial Git y del código/documentación presentes en `develop`. No ejecuta ni certifica las pruebas en esta revisión.
