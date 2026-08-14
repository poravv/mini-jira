# Cómo contribuir

## Flujo de ramas

- `main`: versión estable. `develop`: integración del trabajo aprobado.
- Prohibido trabajar directamente sobre `main` o `develop`.
- Crear la rama desde `develop`: `feature/<nombre>` para funcionalidades, `fix/<nombre>` para correcciones. Ej.: `feature/autenticacion`, `fix/error-validacion-incidencia`.
- El PR siempre apunta a `develop`. A `main` solo se integra desde `develop` con aprobación del líder técnico.

## Commits

Formato: `type: descripción` en minúscula e imperativo.

Tipos: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`.

```
feat: agregar filtro por estado en incidencias
fix: validar título vacío al crear incidencia
```

Commits atómicos: un cambio por commit. No mezclar fix + refactor + feature.

## Requisitos del Pull Request

Cada PR debe incluir (sección 10 del [doc de definición](docs/definicion-proyecto-colaborativo-dev-jr.md)):

- Issue relacionado, descripción del problema y resumen de la solución.
- Evidencia visual o técnica (captura, respuesta de la API, logs).
- Endpoints y cambios de base de datos realizados.
- Validaciones, logs y pruebas incorporadas.
- Riesgos o limitaciones conocidas.
- Documentación actualizada.
- Si la IA influyó materialmente en la solución, indicarlo.

La plantilla de PR (`.github/pull_request_template.md`) incluye la checklist completa.

## Code review (sección 11 del doc)

Cada PR lo revisa primero otro junior; el líder técnico valida al final. El revisor verifica:

- Claridad y legibilidad del código.
- Cumplimiento del requerimiento y respeto de la arquitectura modular.
- Validación de entradas y manejo de errores.
- Seguridad básica (sin secretos, sin datos sensibles en logs).
- Calidad y utilidad de logs y pruebas.
- Duplicaciones y documentación actualizada.

Las observaciones deben ser concretas, respetuosas y justificadas técnicamente. No se aprueba un PR solo porque compila.
