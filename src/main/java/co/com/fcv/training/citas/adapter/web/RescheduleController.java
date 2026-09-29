package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.Ports;
import co.com.fcv.training.citas.application.SchedulingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
class RescheduleController {
    record RescheduleRequestBody(@NotNull Short locationId, @NotNull LocalDateTime startAt) {}
    private final SchedulingService scheduling;
    RescheduleController(SchedulingService scheduling) { this.scheduling = scheduling; }
    @PostMapping("/appointments/{appointmentId}/reschedules") @PreAuthorize("hasRole('USER')")
    Ports.AppointmentView request(@AuthenticationPrincipal Jwt jwt, @PathVariable Long appointmentId, @Valid @RequestBody RescheduleRequestBody body) {
        return scheduling.reschedule(Long.valueOf(jwt.getSubject()), appointmentId, body.locationId(), body.startAt());
    }
}
