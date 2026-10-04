# WF-003 — Resumen operativo diario
 
**Trigger:** Schedule (ej. diario al cierre de jornada, 20:00).

**Flujo:** Consultar API → resumen agrupado del día → formatear tabla HTML → enviar Gmail a la cuenta administrativa/operativa.

## Endpoint de Lectura en citas-api
- **Método:** `GET /api/v1/automation/summary/daily?date=YYYY-MM-DD` (parámetro `date` opcional, por defecto fecha actual).
- **Autenticación:** Header `X-Api-Key: {{ $env.N8N_SERVICE_TOKEN }}` (mínimo 16 caracteres, rol `ROLE_AUTOMATION`).
- **Respuesta (JSON agrupado):**
```json
{
  "date": "2026-10-04",
  "total": 42,
  "rows": [
    {
      "location": "Sede Bucaramanga",
      "specialty": "Cardiología",
      "status": "APPROVED",
      "total": 12
    },
    {
      "location": "Sede Bucaramanga",
      "specialty": "Medicina General",
      "status": "COMPLETED",
      "total": 18
    },
    {
      "location": "Sede Floridablanca",
      "specialty": "Pediatría",
      "status": "NO_SHOW",
      "total": 3
    }
  ]
}
```

## Requisitos y Seguridad
- Endpoint de solo lectura con agregación en memoria sin exponer listas nominales de pacientes ni historias clínicas.
- Métricas consolidadas por sede, especialidad y estado de cita.
- El destinatario de correo se configura en n8n mediante variable `ADMIN_NOTIFICATIONS_EMAIL`.

## Entregable
`WF-003-daily-operational-summary.json`

