# Evidencia S2 — identidad y autenticación

Fecha de validación: 2026-09-24

## Alcance

HU-005, HU-006 y HU-007: registro de USER, login JWT, rotación de refresh y logout bajo `/api/v1/auth`.

## Comandos y resultados

- `docker compose -f compose.test.yml run --rm api-test mvn -q --no-transfer-progress test`
- Resultado: **9 pruebas, 0 fallos, 0 errores**.
- Suites: `AuthIntegrationTest` (5), `AuthRequestGuardTest` (2), `IdentityTest` (2).
- La suite usa MySQL 8.4 temporal mediante Testcontainers y aplica Flyway V1.

## Criterios verificados

- Registro de identidad única, rol `USER` y contraseña almacenada con BCrypt.
- Validación server-side y errores de duplicidad/credencial sin revelar información sensible.
- Access JWT en JSON y refresh JWT en cookie `HttpOnly`.
- Rotación atómica de refresh, rechazo de reutilización y logout con revocación.
- Autorización por rol, origen permitido y encabezado de solicitud requerido.

## Evidencia relacionada

- HU: `docs/FCV Dev/scrum/historias-de-usuario/HU-005-registrar-usuario.md`, `HU-006-iniciar-sesion.md`, `HU-007-renovar-y-cerrar-sesion.md`.
- Contrato: `docs/FCV Dev/llm-wiki/wiki/contracts.md`.
- Migración: `src/main/resources/db/migration/V1__identity.sql`.

## Pendiente fuera de S2

La integración visual completa del cliente y las pantallas de agenda pertenecen a HU-033/S3; no se declaran completadas aquí.
