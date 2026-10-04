# WF-001 — Recordatorio de citas próximas

**Trigger:** Schedule (ej. cada hora o diariamente a las 07:00).

**Objetivo:** consultar citas `APPROVED` dentro de una ventana configurable (ej. próximas 24 h), enviar Gmail al paciente y registrar resultado.

## Endpoint de Lectura en citas-api
- **Método:** `GET /api/v1/automation/appointments/upcoming?hours=24`
- **Autenticación:** Header `X-Api-Key: {{ $env.N8N_SERVICE_TOKEN }}` (mínimo 16 caracteres, rol `ROLE_AUTOMATION`).
- **Respuesta (JSON):**
```json
[
  {
    "appointmentId": 105,
    "patientFirstName": "Carlos",
    "patientEmail": "carlos@example.com",
    "doctorName": "Dr(a). Ana Ruiz",
    "specialty": "Cardiología",
    "location": "Sede Principal",
    "scheduledStartAt": "2026-10-10T10:00:00"
  }
]
```

## Requisitos y Seguridad
- Solo devuelve citas `APPROVED`. No envía recordatorios a citas `CANCELLED` o `REJECTED`.
- Privacidad: no incluye documento de identidad ni diagnósticos/datos clínicos.
- Resiliencia: si la API no está disponible o responde con error, el flujo captura el fallo sin reintentos destructivos.
- Credenciales: las credenciales de Gmail OAuth2 y `N8N_SERVICE_TOKEN` residen exclusivamente en n8n (variables de entorno / credenciales protegidas de n8n). Nunca se incluyen en el repositorio Git.

## Entregable
`WF-001-appointment-reminders.json`

