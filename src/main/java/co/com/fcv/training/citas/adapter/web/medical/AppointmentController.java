package co.com.fcv.training.citas.adapter.web.medical;

import co.com.fcv.training.citas.application.medical.MedicalServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    public record BookRequest(
        @NotNull Short locationId,
        @NotNull Short specialtyId,
        @NotNull Long professionalId,
        @NotNull LocalDateTime scheduledStartAt,
        String reason,
        String referralCode
    ) {}

    public record RescheduleRequest(
        @NotNull LocalDateTime newStartAt,
        Long professionalId,
        String reason
    ) {}

    public record CancelRequest(
        String reason
    ) {}

    public record CompleteRequest(
        String notes
    ) {}

    public record NoShowRequest(
        String reason
    ) {}

    public record RejectRequest(
        @NotBlank(message = "El motivo de rechazo es obligatorio")
        String reason
    ) {}

    private final MedicalServices services;

    public AppointmentController(MedicalServices services) {
        this.services = services;
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<List<MedicalServices.AppointmentDto>> getMyAppointments(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(services.getMyAppointments(userId));
    }

    @PostMapping
    public ResponseEntity<MedicalServices.AppointmentDto> bookAppointment(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody BookRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        MedicalServices.BookAppointmentCommand cmd = new MedicalServices.BookAppointmentCommand(
            request.locationId(),
            request.specialtyId(),
            request.professionalId(),
            request.scheduledStartAt(),
            request.reason(),
            request.referralCode()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(services.bookAppointment(userId, cmd));
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<MedicalServices.AppointmentDto> rescheduleAppointment(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id,
        @Valid @RequestBody RescheduleRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        MedicalServices.RescheduleCommand cmd = new MedicalServices.RescheduleCommand(
            request.newStartAt(),
            request.professionalId(),
            request.reason()
        );
        return ResponseEntity.ok(services.rescheduleAppointment(userId, id, cmd));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id,
        @RequestBody(required = false) CancelRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        String reason = (request != null) ? request.reason() : null;
        services.cancelAppointment(userId, id, reason);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Cita cancelada y cupo liberado exitosamente"
        ));
    }

    // ==================== AGENDA MÉDICA (RF-16, RF-17) ====================

    @GetMapping("/professional/agenda")
    public ResponseEntity<List<MedicalServices.ProfessionalAppointmentDto>> getProfessionalAgenda(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) Long professionalId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false) Short locationId
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(services.getProfessionalAgenda(userId, professionalId, date, locationId));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeAppointment(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id,
        @RequestBody(required = false) CompleteRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        String notes = (request != null) ? request.notes() : null;
        services.completeAppointment(userId, id, notes);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Atención médica finalizada exitosamente"
        ));
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<Map<String, Object>> noShowAppointment(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id,
        @RequestBody(required = false) NoShowRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        String reason = (request != null) ? request.reason() : null;
        services.noShowAppointment(userId, id, reason);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Inasistencia registrada y cupo liberado exitosamente"
        ));
    }

    // ==================== GESTIÓN ADMINISTRADOR (RF-12, RF-18) ====================

    @GetMapping("/admin/pending")
    public ResponseEntity<List<MedicalServices.AdminAppointmentDto>> getAdminPendingRequests(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(services.getAdminPendingRequests());
    }

    @GetMapping("/admin/locations-occupancy")
    public ResponseEntity<List<MedicalServices.LocationOccupancyDto>> getAdminLocationsOccupancy(
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(services.getAdminLocationsOccupancy());
    }

    @PostMapping("/admin/professionals")
    public ResponseEntity<MedicalServices.ProfessionalDto> createProfessional(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody MedicalServices.CreateProfessionalCommand command
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(services.createProfessional(command));
    }

    @PatchMapping("/admin/professionals/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleProfessionalStatus(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id
    ) {
        boolean active = services.toggleProfessionalStatus(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "active", active,
            "message", active ? "Profesional activado exitosamente" : "Profesional desactivado exitosamente"
        ));
    }

    @PostMapping("/admin/locations")
    public ResponseEntity<MedicalServices.LocationDto> createLocation(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody MedicalServices.CreateLocationCommand command
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(services.createLocation(command));
    }

    @PatchMapping("/admin/locations/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleLocationStatus(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Short id
    ) {
        boolean active = services.toggleLocationStatus(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "active", active,
            "message", active ? "Sede habilitada exitosamente" : "Sede inhabilitada exitosamente"
        ));
    }

    @PostMapping("/admin/specialties")
    public ResponseEntity<MedicalServices.SpecialtyDto> createSpecialty(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody MedicalServices.CreateSpecialtyCommand command
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(services.createSpecialty(command));
    }

    @PatchMapping("/admin/specialties/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleSpecialtyStatus(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Short id
    ) {
        boolean active = services.toggleSpecialtyStatus(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "active", active,
            "message", active ? "Especialidad habilitada exitosamente" : "Especialidad inhabilitada exitosamente"
        ));
    }

    @PostMapping("/admin/eps")
    public ResponseEntity<MedicalServices.EpsDto> createEps(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody MedicalServices.CreateEpsCommand command
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(services.createEps(command));
    }

    @PatchMapping("/admin/eps/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleEpsStatus(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id
    ) {
        boolean active = services.toggleEpsStatus(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "active", active,
            "message", active ? "Entidad EPS activada exitosamente" : "Entidad EPS desactivada exitosamente"
        ));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveAppointment(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        services.approveSpecializedAppointment(userId, id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Cita especializada aprobada y confirmada exitosamente"
        ));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectAppointment(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id,
        @Valid @RequestBody RejectRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        services.rejectSpecializedAppointment(userId, id, request.reason());
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Solicitud rechazada y cupo liberado exitosamente"
        ));
    }

    // ==================== AUDITORÍA Y TRAZABILIDAD (HU-032) ====================

    @GetMapping("/{id}/history")
    public ResponseEntity<List<MedicalServices.AppointmentHistoryDto>> getAppointmentHistory(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.ok(services.getAppointmentHistory(userId, id));
    }
}
