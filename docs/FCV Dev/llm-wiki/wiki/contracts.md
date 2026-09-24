# Contratos REST

## HECHO

El PRD exige REST/JSON entre `citas-web` y `citas-api` y validación cross-repo en funcionalidades clave.

## DECISIÓN — 2026-09-17 · HU-004/005/006/007

El contrato inicial cubre solamente autenticación bajo `/api/v1/auth`:

| Operación | Entrada | Éxito |
|---|---|---|
| `POST /register` | JSON `firstName`, `lastName`, `documentType`, `documentNumber`, `email`, `phone`, `password` | `201`, JSON con `id`, datos públicos y rol `USER`, sin contraseña |
| `POST /login` | JSON `email`, `password` | `200`, JSON `accessToken`, `tokenType=Bearer`, `expiresIn`; cookie `refresh_token` |
| `POST /refresh` | Cookie `refresh_token` | `200`, nuevo access en JSON y nueva cookie refresh; la anterior se revoca |
| `POST /logout` | Cookie `refresh_token` | `204`, revocación de la sesión y cookie borrada |

Email se normaliza con trim y minúsculas. Documento es único por `(documentType, documentNumber)` normalizados. La contraseña de registro es obligatoria y se limita a 72 bytes UTF-8 por el límite de BCrypt; sus espacios no se alteran. Solo se permite autoregistro `USER`. Errores: `400` validación, `409` duplicidad, `401` credencial/refresh inválido, `403` rol insuficiente, en formato Problem Details; login no revela qué credencial falló.

Access JWT y refresh JWT usan secretos distintos, tipo explícito y duraciones configurables (valores iniciales: 15 minutos y 7 días). El access lleva `sub` y roles. El refresh lleva `sub` y `jti`; su identificador se guarda solo como hash en una sesión persistida. Un refresh válido rota ambos tokens atómicamente. Logout revoca solo la sesión indicada; un access emitido conserva validez hasta su expiración.

Para sitios distintos, la cookie es `HttpOnly; Secure; SameSite=None`, con `Path=/api/v1/auth`. CORS permite credenciales únicamente al `FRONTEND_ORIGIN` configurado. Login, refresh y logout requieren `Origin` permitido cuando se envía y `X-Requested-With: XMLHttpRequest`; el perfil HTTP local usa cookie `SameSite=Lax` sin `Secure`. El cliente futuro deberá enviar credenciales y ese encabezado, guardar access únicamente según su diseño aprobado y eliminar su estado local al salir.

## DECISIÓN — 2026-09-24 · HU-003

Se aprueba el primer corte de catálogos fijos de solo lectura, consumible directamente por `citas-web` con access JWT:

| Operación | Éxito | Representación |
|---|---|---|
| `GET /api/v1/catalogs/roles` | `200` | `{ id, code, name, description }[]` |
| `GET /api/v1/catalogs/appointment-statuses` | `200` | `{ id, code, name, terminal }[]` |
| `GET /api/v1/catalogs/reschedule-statuses` | `200` | `{ id, code, name, terminal }[]` |
| `GET /api/v1/catalogs/insurance-regimes` | `200` | `{ id, code, name }[]` |
| `GET /api/v1/catalogs/locations` | `200` | `{ id, code, name, address, city, department, active }[]` |

Las cinco rutas requieren autenticación. No se publican operaciones de escritura; un `POST` autenticado sobre un catálogo responde `405`. Los valores se precargan mediante `V2__fixed_catalogs.sql`, incluyendo `HIC` e `ICV` con referencias públicas permitidas. La URL base del frontend queda documentada en `citas-web/CATALOG-CONTRACT.md`; la integración visual se reserva para HU-033 y las HU de agenda.

## DECISIÓN — 2026-09-24 · Afiliación inicial opcional

Para que un visitante pueda elegir cobertura durante el registro, el catálogo de planes activos es público y de solo lectura:

| Operación | Éxito | Acceso | Representación |
|---|---|---|---|
| `GET /api/v1/catalogs/insurance-plans` | `200` | Público | `{ id, epsId, epsCode, epsName, regimeId, regimeCode, regimeName, code, name }[]` |

La respuesta solo incluye planes cuyo plan y EPS están activos. `POST`, `PUT`, `PATCH` y `DELETE` no forman parte del contrato.

`POST /api/v1/auth/register` acepta el campo opcional `insurancePlanId` (entero positivo). Si se omite, no se crea afiliación. Si se informa, el plan debe existir y estar activo; de lo contrario responde `400` en Problem Details y no se crea el usuario. Una selección válida crea una fila en `user_insurance_affiliations` mediante FK, sin duplicar nombres de EPS, régimen o plan en `users`. La afiliación inicial deja `membership_number` nulo; el diligenciamiento posterior pertenece a HU-011 completa.

### Impacto cross-repo antes del cambio REST

- `citas-api`: nuevo `pom.xml`, código de dominio/aplicación/adaptadores, migración Flyway, configuración, pruebas y este contrato.
- `citas-web`: sin cambios en este incremento; HU-033 integrará las cuatro rutas, cookie y errores. Al ser endpoints nuevos, no hay cliente previo que migrar.
- Compatibilidad: `/api/v1` fija la versión de este contrato; cambios posteriores requieren revisión de ambas partes. Migración: esquema inicial de identidad por Flyway. Pruebas: REST, seguridad, persistencia y `mvn test` en backend; prueba cross-repo cuando exista el cliente.

### Evidencia del corte de catálogos

- `citas-api`: migración Flyway V2, adaptadores JPA, servicio/controlador REST y `CatalogIntegrationTest`.
- `citas-web`: `CATALOG-CONTRACT.md` registra rutas, representaciones, autenticación y límites de integración.
- Validación: `CatalogIntegrationTest` 2/2 y suite Maven 11/11 sin fallos.
- Validación adicional: registro sin plan, registro con plan activo, plan inexistente rechazado; frontend carga de catálogo y selección opcional cubiertos por Vitest.

## PREGUNTA ABIERTA

Las rutas, filtros, paginación y formatos de fecha/hora de las demás HU siguen sin contrato aprobado.
