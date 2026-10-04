# WF-002 — Notificación por cambio de estado

**Trigger:** Webhook recibido desde `citas-api` tras confirmarse la transacción en base de datos (`TransactionPhase.AFTER_COMMIT`).

## Eventos emitidos
- Cita especializada `APPOINTMENT_APPROVED` / `APPOINTMENT_REJECTED`.
- Reprogramación automática `APPOINTMENT_RESCHEDULED`.
- Cancelación de cita `APPOINTMENT_CANCELLED`.
- Agendamiento directo con confirmación inmediata `APPOINTMENT_APPROVED`.

## Contrato de Payload (JSON mínimo sin datos sensibles)
```json
{
  "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "eventType": "APPOINTMENT_APPROVED",
  "occurredAt": "2026-10-04T16:00:00Z",
  "appointmentId": 128,
  "statusCode": "APPROVED",
  "patientFirstName": "Carlos",
  "patientEmail": "carlos@example.com",
  "specialty": "Cardiología",
  "location": "Sede Principal",
  "scheduledStartAt": "2026-10-10T10:00:00",
  "reason": null
}
```

## Seguridad y Headers
- **Header de autenticación:** `X-Webhook-Secret: {{ $env.N8N_WEBHOOK_SECRET }}`
- **Respuesta webhook determinista:**
  - 200 OK: `{"status": "received", "eventId": "...", "eventType": "..."}`
  - 401 Unauthorized: `{"status": "rejected", "message": "Secreto de webhook inválido"}`
- Si `N8N_WEBHOOK_URL` está vacío o la integración está deshabilitada en la app, no se realizan llamadas HTTP ni se afecta la operación médica.
- Los reintentos del backend son controlados (backoff exponencial ligero) y los fallos se registran sin exponer secretos ni URLs en logs.

## Entregable
`WF-002-status-notifications.json`

