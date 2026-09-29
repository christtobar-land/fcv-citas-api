package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.Ports;
import co.com.fcv.training.citas.application.SchedulingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/appointments")
class AppointmentController {
    record ReservationRequest(@NotNull Long professionalId, @NotNull Short locationId, @NotNull Short specialtyId,
                              @NotNull LocalDateTime startAt, @Size(max = 500) String reason) {}
    private final SchedulingService scheduling;
    AppointmentController(SchedulingService scheduling) { this.scheduling = scheduling; }
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<Ports.AppointmentView> reserve(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReservationRequest request) {
        Ports.AppointmentView result = scheduling.reserve(Long.valueOf(jwt.getSubject()), new SchedulingService.Reservation(
                request.professionalId(), request.locationId(), request.specialtyId(), request.startAt(), request.reason()));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
