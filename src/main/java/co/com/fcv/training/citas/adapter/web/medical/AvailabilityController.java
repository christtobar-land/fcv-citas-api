package co.com.fcv.training.citas.adapter.web.medical;

import co.com.fcv.training.citas.application.medical.MedicalServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/availability")
public class AvailabilityController {

    public record CreateBlockRequest(
        Long professionalId,
        @NotNull Short locationId,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableDate,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime
    ) {}

    private final MedicalServices services;

    public AvailabilityController(MedicalServices services) {
        this.services = services;
    }

    @GetMapping
    public ResponseEntity<List<MedicalServices.AvailableSlotDto>> getAvailability(
        @RequestParam(required = false) Short locationId,
        @RequestParam(required = false) Short specialtyId,
        @RequestParam(required = false) Long professionalId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now().plusDays(1);
        return ResponseEntity.ok(services.findAvailableSlots(locationId, specialtyId, professionalId, targetDate));
    }

    // ==================== GESTIÓN DE DISPONIBILIDAD MÉDICA (RF-08) ====================

    @GetMapping("/blocks")
    public ResponseEntity<List<MedicalServices.AvailabilityBlockDto>> getProfessionalBlocks(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) Long professionalId
    ) {
        Long userId = (jwt != null && jwt.getSubject() != null) ? Long.valueOf(jwt.getSubject()) : null;
        return ResponseEntity.ok(services.getProfessionalBlocks(userId, professionalId));
    }

    @PostMapping("/blocks")
    public ResponseEntity<MedicalServices.AvailabilityBlockDto> createBlock(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateBlockRequest request
    ) {
        Long userId = (jwt != null && jwt.getSubject() != null) ? Long.valueOf(jwt.getSubject()) : null;
        MedicalServices.CreateBlockCommand cmd = new MedicalServices.CreateBlockCommand(
            request.professionalId(),
            request.locationId(),
            request.availableDate(),
            request.startTime(),
            request.endTime()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(services.createAvailabilityBlock(userId, cmd));
    }

    @DeleteMapping("/blocks/{id}")
    public ResponseEntity<Map<String, Object>> deleteBlock(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long id
    ) {
        Long userId = (jwt != null && jwt.getSubject() != null) ? Long.valueOf(jwt.getSubject()) : null;
        services.deleteAvailabilityBlock(userId, id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Franja de disponibilidad eliminada correctamente"
        ));
    }
}

