# Autenticación JWT

Mini Jira usa tokens JWT firmados con HMAC-SHA256. El endpoint principal es:

```http
POST /api/auth/login
Content-Type: application/json

{"identifier":"admin","password":"admin123"}
```

La respuesta contiene el token, su tipo, la duración en segundos y el usuario autenticado:

```json
{
  "token": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": { "id": 1, "username": "admin", "role": "ADMIN" }
}
```

Las incidencias y las operaciones privadas de usuarios requieren:

```http
Authorization: Bearer eyJ...
```

El registro (`POST /api/users`) es público y siempre crea usuarios con rol `USER`; solo un administrador autenticado puede asignar otro rol. `GET /api/users`, `PUT /api/users/{id}` y `PATCH /api/users/{id}/status` requieren `ADMIN`. El clima, Swagger y el login son públicos.

## Configuración

Definí `JWT_SECRET` con una clave Base64 de al menos 32 bytes. `JWT_ISSUER` identifica la aplicación y `JWT_EXPIRATION_SECONDS` controla la duración del token. Los valores incluidos en el entorno local sirven únicamente para desarrollo; deben reemplazarse en cualquier despliegue real.

El usuario inicial de desarrollo es `admin` / `admin123`, creado por Liquibase en el changeset `003-insert-admin-user`.
