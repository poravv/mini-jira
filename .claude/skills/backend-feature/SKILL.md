---
name: backend-feature
description: Cómo agregar un módulo o endpoint nuevo al backend Spring Boot siguiendo la arquitectura modular, con changeset de Liquibase y pruebas. Usar al crear o modificar funcionalidad en apps/backend.
---

# Agregar un módulo o endpoint al backend

## Estructura obligatoria

Todo módulo nuevo vive en `com.minijira.<modulo>` con esta forma (espejo del módulo `issue` existente):

```
com.minijira.<modulo>/
├── controller/   # endpoints REST, validación de entrada (@Valid)
├── service/      # reglas de negocio, sin anotaciones web
├── repository/   # Spring Data JPA
├── entity/       # entidades JPA
└── dto/          # request/response — nunca exponer entidades en la API
```

## Pasos

1. Mirá los módulos `issue` y `user` como referencia de estilo y estructura.
2. Creá la entidad y su changeset de Liquibase en `src/main/resources/db/changelog/` (un archivo por cambio, incluido en el changelog maestro; numeración correlativa: revisá el último `NNN-` existente antes de crear uno). **Nunca** modifiques un changeset ya aplicado ni uses DDL manual.
3. Creá repository → service → controller, en ese orden. El controller solo traduce HTTP ↔ DTO; la lógica va en el service.
   Si necesitás datos de otro módulo, usá su `Service`; nunca inyectes el `repository` de otro módulo.
4. Endpoints bajo `/api/<recurso>` en plural. Documentalos con anotaciones OpenAPI (aparecen solos en Swagger).
5. Validá la entrada en los DTO (`@NotBlank`, `@Size`, etc.) y manejá errores con el handler global (400/404/500 consistentes).
6. Logs útiles con SLF4J: qué pasó y con qué id. Nunca secretos ni datos sensibles.

## Pruebas (mínimo)

- Test de integración del endpoint (MockMvc o WebTestClient): happy path + un caso de error (validación o 404).
- Test unitario del service solo si tiene lógica no trivial.

## Antes de terminar

- `./mvnw test` en verde y la app levanta con `docker compose up --build`.
- Verificá el endpoint en Swagger y con curl.
- Seguí la skill `pr-ready` antes de abrir el PR.
