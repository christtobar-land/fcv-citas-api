package co.com.fcv.training.citas.application.automation;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento de dominio publicado cuando cambia el estado de una cita y el paciente debe ser notificado.
 * Contrato mínimo hacia n8n (WF-002): sin datos clínicos ni número de documento.
 *
 * @param eventId   UUID único para que n8n pueda aplicar idempotencia
 * @param eventType APPOINTMENT_APPROVED | APPOINTMENT_REJECTED | APPOINTMENT_RESCHEDULED | APPOINTMENT_CANCELLED
 */
public record AppointmentNotificationEvent(
        String eventId,
        String eventType,
        OffsetDateTime occurredAt,
        Long appointmentId,
        String statusCode,
        String patientFirstName,
        String patientEmail,
        String specialty,
        String location,
        String scheduledStartAt,
        String reason
) {
    public static final String APPROVED = "APPOINTMENT_APPROVED";
    public static final String REJECTED = "APPOINTMENT_REJECTED";
    public static final String RESCHEDULED = "APPOINTMENT_RESCHEDULED";
    public static final String CANCELLED = "APPOINTMENT_CANCELLED";

    public static String newId() {
        return UUID.randomUUID().toString();
    }
}
