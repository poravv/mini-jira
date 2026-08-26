---
name: pr-ready
description: Checklist obligatoria antes de abrir un Pull Request, según la sección 10 del documento de definición. Usar siempre antes de crear un PR.
---

# Preparar un Pull Request

## Antes de abrir el PR, verificá

1. **Rama y destino**: tu rama es `feature/...` o `fix/...` creada desde `develop`, y el PR apunta a `develop` (nunca a `main`).
2. **Compila y corre**: `docker compose up --build` levanta todo y la funcionalidad se puede probar localmente.
3. **Pruebas**: `./mvnw test` (backend) y/o `npm test` (frontend) en verde, con tests nuevos para tu cambio.
4. **Sin secretos**: revisá `git diff develop` completo — sin credenciales, tokens, `.env` ni archivos generados.
5. **Validaciones y errores**: entradas validadas, errores esperados manejados (400/404/500 coherentes).
6. **Logs**: útiles para diagnóstico, sin datos sensibles.
7. **Documentación**: README/Swagger/docs actualizados si el cambio los afecta.
8. **Commits**: atómicos, con formato `type: descripción`.

## Al escribir el PR

Completá la plantilla (`.github/pull_request_template.md`) entera:

- Issue relacionado, problema y resumen de la solución.
- Endpoints y cambios de BD (changesets de Liquibase).
- Evidencia: captura, respuesta de la API o logs.
- Cómo usaste la IA, si influyó materialmente.
- Riesgos o limitaciones conocidas.

## Regla final

Tenés que poder explicar cada línea del PR. Si no podés explicar algo, no está listo para revisión.
