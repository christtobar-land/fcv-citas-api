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

## Integración con n8n (Automatizaciones y Notificaciones)

El sistema soporta integración desacoplada y segura con n8n mediante eventos asíncronos y endpoints de consulta protegidos. **Ninguna credencial de n8n, token ni secreto de Gmail OAuth reside en el código ni en el repositorio.**

### Flujos Soportados

1. **WF-001 — Recordatorios de Citas Próximas (Schedule Trigger):**
   - n8n consulta periódicamente `GET /api/v1/automation/appointments/upcoming?hours=24`.
   - Autenticado mediante header `X-Api-Key` (validado contra `N8N_SERVICE_TOKEN`).
   - Envía recordatorios por correo a los pacientes con citas confirmadas (`APPROVED`).

2. **WF-002 — Notificaciones por Cambio de Estado (Webhook Trigger):**
   - Tras confirmarse una transacción (`AFTER_COMMIT`), la API emite un evento POST asíncrono hacia `N8N_WEBHOOK_URL`.
   - Incluye header `X-Webhook-Secret` para autenticidad.
   - Eventos soportados: aprobación, rechazo, reprogramación y cancelación.
   - Payload mínimo de solo lectura: no expone datos clínicos ni números de documento.

3. **WF-003 — Resumen Operativo Diario (Schedule Trigger):**
   - n8n consulta `GET /api/v1/automation/summary/daily`.
   - Devuelve métricas agregadas por sede, especialidad y estado sin exponer listas de pacientes.
   - Envía consolidado diario al correo administrativo configurado en n8n.

### Variables de Entorno (en `.env`)

```bash
# Activar o desactivar la integración globalmente
N8N_ENABLED=true

# URL pública o de túnel del webhook de n8n (para WF-002)
N8N_WEBHOOK_URL=https://tu-instancia-n8n.com/webhook/medihealth-appointment-status

# Secreto compartido acordado entre la API y n8n
N8N_WEBHOOK_SECRET=tu_secreto_compartido_aqui

# Token de servicio que n8n envía en el header X-Api-Key para leer datos (WF-001 y WF-003)
N8N_SERVICE_TOKEN=tu_token_de_servicio_minimo_16_caracteres

# Tiempos de espera y reintentos para llamadas hacia n8n
N8N_TIMEOUT_SECONDS=5
N8N_MAX_ATTEMPTS=3
```

### Configuración en n8n Web y Conexión con Localhost

- **Google / Gmail OAuth2:** La conexión de correo se crea exclusivamente en el gestor de credenciales de n8n (`Credentials > New > Gmail OAuth2`). La app jamás tiene acceso a credenciales de Google.
- **Acceso a la API local desde n8n Web:** Dado que la instancia de n8n se ejecuta en la nube/web y la API corre en `localhost:8080`, para que n8n pueda consultar los endpoints `/api/v1/automation/**` se expone temporalmente el puerto de la API usando una herramienta de túnel seguro (como Cloudflare Tunnel `cloudflared` o `ngrok http 8080`) o desplegando la API en un entorno con IP/dominio público. Las llamadas salientes de la API hacia n8n (WF-002) funcionan directamente hacia la URL web de n8n.
- **Plantillas de flujos:** En el directorio `automations/n8n/` se encuentran las especificaciones y plantillas JSON listas para importar en n8n (`WF-001-appointment-reminders.json`, `WF-002-status-notifications.json`, `WF-003-daily-operational-summary.json`).

