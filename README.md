# citas-api

Repositorio backend del proyecto. **No contiene implementación de negocio inicial**.

## Debe ser construido por el estudiante
- Java 21 + Spring Boot 3.5.x + Maven.
- Arquitectura hexagonal.
- MySQL + Flyway.
- Spring Security + JWT access/refresh.
- REST.
- Pruebas.

## Documentación compartida
- `docs/FCV Dev/scrum/`: épicas/HU del proyecto.
- `docs/FCV Dev/llm-wiki/`: única LLM Wiki global del workspace.
- `docs/FCV Dev/subagents/`: catálogo operativo de subagentes del orquestador.
- `automations/n8n/`: JSON exportados en S5/S6.

Lee el PRD en la carpeta raíz antes de continuar Spring Boot.

## Incremento de identidad backend

Este incremento implementa HU-005/006/007 por REST; no incluye recuperación de contraseña. El contrato está en `docs/FCV Dev/llm-wiki/wiki/contracts.md`. Java 21/Spring Boot 3.5.0/Maven y la migración Flyway V1 se ejecutan en `develop`.

Para desarrollo local, configura las variables de `.env.example` con valores propios fuera de Git y activa el perfil `local`. Los secretos JWT deben ser distintos y tener al menos 32 bytes. El perfil local usa cookie HTTP `SameSite=Lax`; el predeterminado requiere HTTPS y usa `SameSite=None; Secure`.

En Windows con Docker Desktop, ejecuta las pruebas desde este directorio:

```powershell
docker compose -f compose.test.yml run --rm api-test mvn test
```

El contenedor Maven usa Java 21; Testcontainers crea un MySQL 8.4 temporal. No requiere Java/Maven instalados en el host.

## Datos semilla para pruebas funcionales

La base de datos arranca **limpia**: Flyway (al iniciar la API) solo crea el esquema y los **catálogos** (2 sedes, 12 especialidades, EPS/planes, roles, estados). No hay usuarios en el código. Para probar todos los flujos se carga un script de inserción aparte:

1. Arranca MySQL y la API una vez (Flyway migra el esquema).
2. Ejecuta el seed:

```powershell
.\scripts\seed-db.ps1 -Container <contenedor-mysql> -RootPassword "<clave-root>" -Database citas_fcv_training
```

Sin Docker: `mysql -u root -p --default-character-set=utf8mb4 citas_fcv_training < database/seed/seed-test-data.sql`

| Rol | Correo | Detalle |
|---|---|---|
| Administrador | `admin1@medih.com` | Gestión de profesionales, aprobaciones y catálogos |
| Médico | `medico1@medih.com` … `medico12@medih.com` | **Sede 1 · El Bosque**: 1 médico por cada especialidad |
| Médico | `medico13@medih.com` … `medico24@medih.com` | **Sede 2 · Norte**: 1 médico por cada especialidad |

- Contraseña de todas las cuentas semilla: `medih123`.
- Cada médico tiene **una sola sede y una sola especialidad** (1 Medicina General, 2 Cardiología Adulto, 3 Cardiología Pediátrica, 4 Medicina Interna, 5 Pediatría, 6 Nefrología, 7 Urología, 8 Gastroenterología, 9 Neumología Adulto, 10 Endocrinología, 11 Ortopedia y Traumatología, 12 Neurología).
- Agenda: lunes a sábado, 07:00–12:00 y 14:00–17:00, desde el día siguiente hasta el **31 de octubre de 2026**. Después, cada médico publica su agenda desde su portal.
- **Pacientes**: no hay semilla; se registran desde la UI (`/register`) y quedan con rol `USER`.
- El script es idempotente: re-ejecutarlo no duplica datos.
