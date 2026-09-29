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

## DECISIÓN — 2026-09-24 · Oferta administrable (HU-014 a HU-017)

El corte S4 incorpora gestión ADMIN de especialidades, profesionales y sus asociaciones. Las rutas requieren access JWT con rol `ADMIN`; no se publican en el cliente como operaciones de usuario final.

| Operación | Entrada | Éxito | Reglas principales |
|---|---|---|---|
| `GET /api/v1/admin/specialties` | — | `200` con especialidades | Incluye activas e inactivas para administración. |
| `POST /api/v1/admin/specialties` | `code`, `name`, `appointmentDurationMinutes`, `general`, `requiresAdminApproval` | `201` | Duración solo 30/60; una especialidad general no requiere aprobación adicional. |
| `PATCH /api/v1/admin/specialties/{id}` | Campos anteriores opcionales | `200` | Actualización lógica; no se borra físicamente una especialidad referenciada. |
| `POST /api/v1/admin/professionals` | identidad sintética, `temporaryPassword`, `professionalCode`, `licenseNumber` | `201` | Crea identidad con rol `PROFESSIONAL`; solo ADMIN; unicidad de email/documento/código/matrícula. |
| `PUT /api/v1/admin/professionals/{id}/specialties` | `specialtyIds`, `primarySpecialtyId` | `204` | Una o más especialidades activas; la primaria debe pertenecer a la selección. |
| `PUT /api/v1/admin/professionals/{id}/locations` | `locationIds` | `204` | Una o ambas sedes del catálogo fijo; máximo dos y todas activas. |
| `PATCH /api/v1/admin/professionals/{id}/active` | `{ "active": boolean }` | `204` | El estado se conserva para las reglas futuras de disponibilidad. |

Las operaciones de asociación reemplazan atómicamente la selección anterior y mantienen relaciones normalizadas. La duración de la especialidad es la única fuente de slots; la consulta de disponibilidad y la reserva todavía no forman parte de este corte.

## DECISIÓN — 2026-09-29 · Agenda, disponibilidad y reservas (HU-018 a HU-024)

Las fechas se representan como `YYYY-MM-DD`, las horas como `HH:mm[:ss]` y los instantes como `YYYY-MM-DDTHH:mm:ss`, interpretados en `America/Bogota`. Las franjas son futuras y sus límites siempre caen en intervalos de 30 minutos.

| Operación | Acceso | Éxito | Regla relevante |
|---|---|---|---|
| `GET /api/v1/catalogs/specialties` | Autenticado | `200` | Solo especialidades activas para búsqueda. |
| `GET|POST /api/v1/professional/availability-blocks` | PROFESSIONAL | `200`/`201` | Consulta solo bloques propios; creación valida sede asignada, estado activo, futuro y no solapamiento. |
| `PATCH|DELETE /api/v1/professional/availability-blocks/{id}` | PROFESSIONAL dueño | `200`/`204` | No permite bloques pasados, ajenos o con slots comprometidos. |
| `GET /api/v1/availability?locationId=&specialtyId=&date=&professionalId?` | USER | `200` | Solo profesionales/sedes/especialidades vigentes y slots completos consecutivos. |
| `POST /api/v1/appointments` | USER | `201` | Reserva atómica; general crea `APPROVED`, especializada crea `REQUESTED`; conflicto de slot devuelve `409`. |
| `GET /api/v1/admin/appointments` | ADMIN | `200` | Devuelve únicamente solicitudes `REQUESTED`. |
| `POST /api/v1/admin/appointments/{id}/decision` | ADMIN | `200` | `APPROVE` conserva slots; `REJECT` exige `reason`, libera slots y deja `REJECTED`. |

La migración V5 mantiene 3FN: bloques, slots, citas e historial son entidades separadas. Los slots referencian la cita cuando están retenidos u ocupados; las transiciones se registran en historial con fuente `SYSTEM`, `USER` o `ADMIN`. El bloqueo pesimista de los slots durante la reserva impide dos asignaciones al mismo horario.

### Impacto cross-repo antes del cambio REST

- `citas-api`: entidades/puertos/servicios/adaptadores ADMIN, migración Flyway V4, pruebas y este contrato.
- `citas-web`: cliente `adminOfferApi`, pantalla ADMIN para crear/configurar oferta y pruebas Vitest.
- Compatibilidad: `/api/v1` fija la versión de este contrato; cambios posteriores requieren revisión de ambas partes. Migración: esquema inicial de identidad por Flyway. Pruebas: REST, seguridad, persistencia y `mvn test` en backend; prueba cross-repo cuando exista el cliente.

### Evidencia del corte de catálogos

- `citas-api`: migración Flyway V2, adaptadores JPA, servicio/controlador REST y `CatalogIntegrationTest`.
- `citas-web`: `CATALOG-CONTRACT.md` registra rutas, representaciones, autenticación y límites de integración.
- Validación: `CatalogIntegrationTest` 2/2 y suite Maven 11/11 sin fallos.
- Validación adicional: registro sin plan, registro con plan activo, plan inexistente rechazado; frontend carga de catálogo y selección opcional cubiertos por Vitest.

### Evidencia del corte de oferta administrable

- `citas-api`: `V4__offer_catalog_and_professionals.sql`, adaptador JPA, `AdminOfferController` y `OfferIntegrationTest`.
- `citas-web`: `src/admin/adminOfferApi.ts`, `AdminOfferScreen.tsx` y pruebas de rutas/headers.
- Validación: `OfferIntegrationTest` 2/2; regresión `AuthIntegrationTest` 6/6 y `CatalogIntegrationTest` 2/2; frontend 13/13, lint y build PASS.
- Pendiente explícito: CA-02 de HU-014, CA-03 de HU-016 y CA-03 de HU-017 requieren implementar disponibilidad/reserva para probar el efecto de la oferta.

## DECISIÓN — 2026-09-29 · Ciclo de vida básico de cita (HU-025, HU-026, HU-029, HU-030 y HU-032)

El corte amplía la consulta y operación de citas ya reservadas. Las rutas requieren access JWT y verifican ownership dentro del caso de uso; un identificador ajeno no revela su auditoría ni permite una transición.

| Operación | Acceso | Éxito | Regla relevante |
|---|---|---|---|
| `GET /api/v1/appointments?status=&date=` | USER | `200` | Devuelve solo citas propias; cada elemento incluye sede, especialidad, profesional, horarios, estado y motivo cuando exista. |
| `POST /api/v1/appointments/{id}/cancel` | USER dueño | `200` | Solo cita futura no terminal; cambia a `CANCELLED`, libera slots y agrega historial `USER`. |
| `GET /api/v1/appointments/{id}/history` | USER dueño, PROFESSIONAL asignado o ADMIN | `200` | Historial de estados sin información clínica. |
| `GET /api/v1/professional/appointments?date=&locationId=` | PROFESSIONAL | `200` | Solo agenda propia en estado `APPROVED`. |
| `POST /api/v1/professional/appointments/{id}/close` | PROFESSIONAL asignado | `200` | Solo tras terminar; acepta `COMPLETED` o `NO_SHOW`, y registra como actor al usuario profesional con fuente `USER`. |

La fuente `USER` del cierre es una decisión explícitamente aprobada: el profesional actúa como un usuario autenticado y el historial conserva su identificador. Reprogramación y recuperación de contraseña siguen fuera de este corte.

## PREGUNTA ABIERTA

Paginación, perfil completo, cancelación, reprogramación, agenda de citas, cierre y automatizaciones continúan sin contrato final.
