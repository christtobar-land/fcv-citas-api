package co.com.fcv.training.citas.adapter.web;

import co.com.fcv.training.citas.application.Ports;
import co.com.fcv.training.citas.application.SchedulingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
class RescheduleController {
    record RescheduleRequestBody(@NotNull Short locationId, @NotNull LocalDateTime startAt) {}
    record DecisionBody(@NotBlank @Pattern(regexp = "APPROVE|REJECT") String decision, @Size(max = 500) String reason) {}
    private final SchedulingService scheduling;
    RescheduleController(SchedulingService scheduling) { this.scheduling = scheduling; }
    @PostMapping("/appointments/{appointmentId}/reschedules") @PreAuthorize("hasRole('USER')")
    Ports.RescheduleView request(@AuthenticationPrincipal Jwt jwt, @PathVariable Long appointmentId, @Valid @RequestBody RescheduleRequestBody body) { return scheduling.requestReschedule(Long.valueOf(jwt.getSubject()), appointmentId, body.locationId(), body.startAt()); }
    @GetMapping("/reschedules") @PreAuthorize("hasRole('USER')")
    List<Ports.RescheduleView> mine(@AuthenticationPrincipal Jwt jwt) { return scheduling.myReschedules(Long.valueOf(jwt.getSubject())); }
    @GetMapping("/admin/reschedules") @PreAuthorize("hasRole('ADMIN')")
    List<Ports.RescheduleView> pending() { return scheduling.pendingReschedules(); }
    @PostMapping("/admin/reschedules/{id}/decision") @PreAuthorize("hasRole('ADMIN')")
    Ports.RescheduleView decide(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody DecisionBody body) { return scheduling.decideReschedule(Long.valueOf(jwt.getSubject()), id, body.decision(), body.reason()); }
}
